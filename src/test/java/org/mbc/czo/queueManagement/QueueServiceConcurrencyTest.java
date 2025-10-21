package org.mbc.czo.queueManagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbc.czo.function.apiQueueManagement.service.ApiQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class QueueServiceConcurrencyTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ApiQueueService queueService;

    private final Long itemId = 3652L;

    private static final String WAIT_KEY = "queue:wait:";

    @BeforeEach
    void setup() {
        // Redis 초기화
        redisTemplate.delete(WAIT_KEY + itemId);
    }


    @Test
    void testConcurrentQueueRegistration() throws InterruptedException {
        int threadCount = 10000;   // 동시에 등록할 사용자 수
        int poolSize = 200;       // 스레드 풀 크기

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    queueService.joinQueue(itemId, userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // 결과 검증
        Long totalUsersInQueue = redisTemplate.opsForZSet().zCard(WAIT_KEY+itemId);
        System.out.println("대기열 등록 완료 인원: " + totalUsersInQueue);

        // 상위 5명 샘플 확인
        Set<ZSetOperations.TypedTuple<String>> sample = redisTemplate.opsForZSet().rangeWithScores(WAIT_KEY+itemId, 0, 4);
        System.out.println("대기열 상위 5명 샘플: " + sample);

        //assertThat(totalUsersInQueue).isEqualTo(threadCount);
    }
}

