package org.mbc.czo.function.apiQueueManagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.common.redisLock.AtomicOperation;
import org.mbc.czo.function.common.redisLock.RedisAtomicOperationWithLock;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApiQueueService {

    private static final String WAIT_KEY_PREFIX = "queue:wait:";    // itemId 기준
    private static final String TICKET_KEY_PREFIX = "queue:ticket:"; // itemId 기준

    private static final int MAX_ACTIVE = 300;
    private static final int QUEUE_TTL = 3600;

    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisAtomicOperationWithLock redisAtomicOperationWithLock;

    // 대기열 등록 (itemId 기준)
    public void joinQueue(Long itemId, String userId) {
        String queueKey = WAIT_KEY_PREFIX + itemId;
        String seqKey = "queue:seq:" + itemId;
        String lockKey = "lock:queue:" + itemId + ":" + userId;


        AtomicOperation<Void> operation = () -> {
            Double score = redisTemplate.opsForZSet().score(queueKey, userId);
            //대기순번 없으면 추가
            if (score == null) {
                RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
                if (factory == null)
                    throw new IllegalStateException("RedisConnectionFactory = null");

                RedisAtomicLong seq = new RedisAtomicLong(seqKey, factory);
                //증가
                long newScore = seq.incrementAndGet();

                //queue:item:1001 / user-1 / 1.0 이런식으로 저장
                //유저가 어떤 item 대기 중인지 빠르게 조회
                redisTemplate.opsForZSet().add(queueKey, userId, (double) newScore);
                seq.expire(QUEUE_TTL, TimeUnit.SECONDS);

                //역인덱스 저장
                //user-1:3652, user-3:4123 (itemId)
                redisTemplate.opsForHash().put("queue:userToItem", userId, itemId.toString());
                //현재 활성화된 아이템들
                redisTemplate.opsForSet().add("queue:itemIds", itemId.toString());

                log.info("[Queue] {} 대기열 등록 → itemId: {}, score: {}", userId, itemId, newScore);
            }
            return null;
        };

        redisAtomicOperationWithLock.executeAtomicOperationWithLock(lockKey, 3, 10, operation);
    }

    // SSE 구독
    // subscribe = “이 유저가 이벤트 받을 준비가 되었다”
    public SseEmitter sseSubscribe(String userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        return emitter;
    }

    // 대기열 처리 (itemId 기준)
    @Scheduled(fixedRate = 1000)
    public void processWaitingQueues() {
        //대기열이 존재하는 itemId
        Set<String> queueItemIds = redisTemplate.opsForSet().members("queue:itemIds");

        if (queueItemIds == null || queueItemIds.isEmpty()) return;

        for (String itemIdStr : queueItemIds) {

            Long itemId = Long.parseLong(itemIdStr.trim());
            String queueKey = WAIT_KEY_PREFIX + itemId;
            String ticketKey = TICKET_KEY_PREFIX + itemId;

            int min = MAX_ACTIVE / 2;
            int max = MAX_ACTIVE;
            int randomActivate = ThreadLocalRandom.current().nextInt(min, max + 1);

            Long queueSize = redisTemplate.opsForZSet().size(queueKey);
            //log.info("processWaitingQueues2 - 대기열 크기: {} (itemId: {})", queueSize, itemId);

            //ZSet에서 score 순으로 상위 randomActivate명 선택
            Set<ZSetOperations.TypedTuple<String>> nextUsers =
                    redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, randomActivate - 1);

            if (nextUsers == null || nextUsers.isEmpty()) continue;
            log.info("nextUsers:{}", nextUsers);
            for (ZSetOperations.TypedTuple<String> nextUser : nextUsers) {
                String userId = nextUser.getValue();
                if (userId == null) continue;

                // 대기열에서 제거
                redisTemplate.opsForZSet().remove(queueKey, userId);
                // 활성화(입장 가능한 상태)인 유저 ID 목록을 저장
                redisTemplate.opsForSet().add(ticketKey, userId);
                // 역인덱스 제거
                redisTemplate.opsForHash().delete("queue:userToItem", userId);

                //유저에게 입장 가능 알림 전송
                sseSendAsync(userId);
                log.info("[QueueScheduler] {} → active 등록 완료 (itemId: {})", userId, itemId);
            }

          /*  Long activeSize = redisTemplate.opsForSet().size(ticketKey);
            log.info("[QueueScheduler] 처리 종료 후 active 수 (itemId: {}): {}", itemId, activeSize);*/
        }
    }


    //sse 비동기처리
    @Async
    public void sseSendAsync(String userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("queue-enter").data("ENTER"));
                emitter.complete();
                log.info("[QueueScheduler] {}에게 입장 알림 전송 완료", userId);
            } catch (IOException e) {
                log.warn("[QueueScheduler] {} 알림 실패", userId, e);
            }
        } else {
            log.info("[QueueScheduler] {} emitter 없음", userId);
        }
    }

    // 순번 조회
    public Long getQueuePosition(String userId) {
        //역 인덱스 참조
        String itemIdStr = (String) redisTemplate.opsForHash().get("queue:userToItem", userId);
        if (itemIdStr == null) {
            sseSendAsync(userId);
            return 0L; // 대기열 없음
        }
        Long itemId = Long.valueOf(itemIdStr);
        String queueKey = WAIT_KEY_PREFIX + itemId;
        String ticketKey = TICKET_KEY_PREFIX + itemId;

        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        if (rank != null){
            return rank + 1;
        }

        Boolean isActive = redisTemplate.opsForSet().isMember(ticketKey, userId);
        if (isActive != null && isActive) {
            return -1L;
        }
        return 0L;
    }

}
