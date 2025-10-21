package org.mbc.czo.function.apiStockManagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.common.redisLock.AtomicOperation;
import org.mbc.czo.function.apiStockManagement.constant.StockRedisCal;
import org.mbc.czo.function.apiStockManagement.dto.stockCheck.StockCheckReq;
import org.mbc.czo.function.apiStockManagement.kafka.producer.StockDBUpdateProducer;
import org.mbc.czo.function.apiStockManagement.kafka.producer.StockReissueProducer;
import org.mbc.czo.function.common.redisLock.RedisAtomicOperationWithLock;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.exception.OutOfStockException;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Log4j2
@Service("ApiStockService")
@RequiredArgsConstructor // final붙은 필드를 생성자로
public class ApiStockService {

    private final ItemRepository itemRepository;

    private final RedisTemplate<String, Long> redisTemplate;

    private final RedissonClient redissonClient;

    private final StockDBUpdateProducer stockDBUpdateProducer;
    private final StockReissueProducer stockReissueProducer;

    private final RedisAtomicOperationWithLock redisAtomicOperationWithLock;

    //추후 외부에서 세팅 가능하도록 수정
    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final String REISSUE_LOCK_KEY_PREFIX = "REISSUED_LOCK:";
    private static final int REISSUE_THRESHOLD = 10; // 임계 재고
    private static final int REISSUE_LOCK_TTL = 86400;  // 초

    private static final int PRODUCT_TTL = 86400; //24시간

    // RedisAtomicLong 캐싱 (스레드 안전)
    private final Map<Long, RedisAtomicLong> atomicStockMap = new ConcurrentHashMap<>();

    @Transactional
    public void checkAndDecreaseStock(StockCheckReq stockCheckReq) {

        // 1. Redis 재고 감소
        // 코드 확장, 유연성을 위해 Supplier 사용
        int remainingStock = updateStockRedis(
                stockCheckReq.getId(),
                stockCheckReq.getQuantity(),
                StockRedisCal.DECREASE,
                stockCheckReq.getInitialStockSupplier()
        );

        // 2. 임계치 도달시 재발주 이벤트 처리
        if (remainingStock <= REISSUE_THRESHOLD) {
            sendStockReissueEventWithLock(stockCheckReq.getId(), remainingStock);
        }
    }

    @Transactional
    public void checkAndIncreaseStock(StockCheckReq stockCheckReq) {
        long productId = stockCheckReq.getId();
        int quantity = stockCheckReq.getQuantity();

        // 1. Redis 재고 증가
        int remainingStock = updateStockRedis(
                stockCheckReq.getId(),
                stockCheckReq.getQuantity(),
                StockRedisCal.INCREASE,
                stockCheckReq.getInitialStockSupplier()
        );
    }


    // Redis 재고 처리 (감소/증가)
    // initialStockSupplier = 초기 재고를 제공하는 함수
    public int updateStockRedis(
            Long productId, int quantity, StockRedisCal stockRedisCal, Supplier<Integer> initialStockSupplier) {

        String stockKey = STOCK_KEY_PREFIX + productId;

        // 락 안에서 Redis 원자적 연산 수행
        AtomicOperation<Long> operation = () -> {

            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null)
                throw new IllegalStateException("RedisConnectionFactory = null");

            RedisAtomicLong atomicStock = new RedisAtomicLong(stockKey, factory);

            // TTL 확인
            Long ttl = redisTemplate.getExpire(stockKey, TimeUnit.SECONDS);
            // Redis 초기화
            // TTL이 없거나(-1), 이미 만료된 경우(-2) → DB 값으로 초기화
            if (ttl == null || ttl <= 0){

                int initialStock = initialStockSupplier.get(); // DB 조회 락 안에서 수행
                atomicStock.set(initialStock);
                atomicStock.expire(PRODUCT_TTL, TimeUnit.SECONDS);
                log.info("initialStock: {}", initialStock);
            }

            long currentStock = atomicStock.get();
            log.info("currentStock: {}", currentStock);
            if (stockRedisCal == StockRedisCal.DECREASE && currentStock < quantity) {
                throw new OutOfStockException("재고 부족: " + productId);
            }

            // 재고 감소/증가
            return stockRedisCal == StockRedisCal.DECREASE
                    ? atomicStock.addAndGet(-quantity)
                    : atomicStock.addAndGet(quantity);
        };

        long updatedStock =
                redisAtomicOperationWithLock.executeAtomicOperationWithLock("lock:" + productId, 3, 10, operation);

        // Kafka 이벤트는 락 해제 후 비동기 발송 (속도 최적화)
        stockDBUpdateProducer.sendStockDBUpdateEvent(productId, (int) updatedStock);

        return (int) updatedStock;
    }

    private void sendStockReissueEventWithLock(Long productId, int remainingStock) {

        String lockKey = REISSUE_LOCK_KEY_PREFIX + productId;

        //락 객체 생성
        RLock lock = redissonClient.getLock(lockKey);

        //락획득 여부 (락 획득 시 다른 스레드의 접근 거부)
        boolean lockAcquired = false;
        try {
            // 락 획득 시도 (0초 기다리고, TTL -> REISSUED_LOCK_TTL)
            lockAcquired = lock.tryLock(0, REISSUE_LOCK_TTL, TimeUnit.SECONDS);

            if (!lockAcquired) {
                // 재발주 이벤트 발송
                log.warn("재발주 이벤트 발송 중, 락 획득 실패: productId={}", productId);
                return;
            }
            // 이벤트 발송
            stockReissueProducer.sendStockReissueEvent(productId, remainingStock, REISSUE_THRESHOLD);

            //다른 스레드로부터 interrupt 호출 받으면 대기 중단
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            log.error("재발주 이벤트 락 획득 중 인터럽트 발생: " + e.getMessage());

        } finally {

            //lock.isHeldByCurrentThread() = 현재 코드 실행 중인 스레드가 락을 가지고 있는가
            if (lockAcquired && lock.isHeldByCurrentThread()) {
                //락 해제
                lock.unlock();
            }
        }
    }


}
