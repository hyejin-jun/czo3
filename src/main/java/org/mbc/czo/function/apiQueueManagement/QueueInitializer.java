package org.mbc.czo.function.apiQueueManagement;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class QueueInitializer {

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void initQueue() {
   /*     String WAIT_KEY = "queue:wait";
        String TICKET_KEY = "queue:ticket";

        // 기존 값 초기화
        redisTemplate.delete(WAIT_KEY);
        redisTemplate.delete(TICKET_KEY);

        // 10,000명 대기열 미리 넣기 (ZSet)
        for (int i = 1; i <= 5000; i++) {
            redisTemplate.opsForZSet().add(WAIT_KEY, "user-" + i, (double)i);
        }

        // TICKET_KEY를 Set으로 초기화 (빈 Set)
        // 최초에는 아무 값도 넣지 않아도 됨, add 시 자동 생성됨

        // 🔹 디버깅용: 대기열 상태 확인
        Long count = redisTemplate.opsForZSet().zCard(WAIT_KEY);
        log.info("대기열 총 인원: {}", count);

        // 🔹 상위 5명 샘플 확인
        Set<ZSetOperations.TypedTuple<String>> queueSample = redisTemplate.opsForZSet().rangeWithScores(WAIT_KEY, 0, 4);
        log.info("대기열 샘플: {}", queueSample);*/
    }

}
