package org.mbc.czo.function.common.redisLock;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisAtomicOperationWithLock {

    private final RedissonClient redissonClient;

    public <T> T executeAtomicOperationWithLock(String key, long waitTimeSec, long leaseTimeSec, AtomicOperation<T> operation) {
        RLock lock = redissonClient.getLock(key);
        boolean lockAcquired = false;

        try {
            // 락 획득 시도
            lockAcquired = lock.tryLock(waitTimeSec, leaseTimeSec, TimeUnit.SECONDS);
            if (!lockAcquired) {
                throw new IllegalStateException("락 획득 실패: " + key);
            }

            // 락 안에서 실행할 원자적 연산 호출
            return operation.executeAtomicOperation();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
