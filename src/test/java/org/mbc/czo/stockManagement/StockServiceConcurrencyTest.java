package org.mbc.czo.stockManagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbc.czo.function.apiStockManagement.dto.stockCheck.StockCheckReq;
import org.mbc.czo.function.apiStockManagement.dto.stockDBUpdateEvent.StockDBUpdateEvent;
import org.mbc.czo.function.apiStockManagement.kafka.producer.StockDBUpdateProducer;
import org.mbc.czo.function.apiStockManagement.service.ApiStockService;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.exception.OutOfStockException;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StockServiceConcurrencyTest {

    @Autowired
    private ApiStockService stockService;

    @Autowired
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StockDBUpdateProducer stockDBUpdateProducer;

    private Long productId;
    private final int initialStockNum = 3990;
    private static final int PRODUCT_TTL = 86400;

    // Kafka 이벤트 처리 완료 대기용 latch
    private CountDownLatch kafkaLatch;

    @BeforeEach
    void setup() {
        redisLongTemplate.getConnectionFactory().getConnection().flushDb();

        // DB 초기화
        Item item = new Item();
        item.setItemNm("10000명 동시주문 테스트 상품");
        item.setPrice(1000);
        item.setStockNumber(initialStockNum);
        item.setItemDetail("동시성 테스트용 상품 10000명 단일 스레드 + 비동기 DB업데이트");
        item = itemRepository.save(item);
        productId = item.getId();

        // Redis 초기화
        String stockKey = "stock:" + productId;
        RedisAtomicLong atomicStock = new RedisAtomicLong(stockKey, redisLongTemplate.getConnectionFactory());
        redisLongTemplate.delete(stockKey);
        atomicStock.set(initialStockNum);
        atomicStock.expire(PRODUCT_TTL, TimeUnit.SECONDS);
    }

    // Kafka Listener
/*    @KafkaListener(topics = "stock-DBUpdate-events", groupId = "test-group-111")
    public void testListener(StockDBUpdateEvent event) {
        // DB 업데이트
        Item item = itemRepository.findById(event.getId())
                .orElseThrow(() -> new IllegalStateException("상품 없음"));
        item.setStockNumber(event.getRemainStock());
        itemRepository.save(item);

        // 이벤트 처리 완료 카운트 감소
        kafkaLatch.countDown();
    }*/

    @Test
    void testConcurrentOrdersWithRealKafka() throws InterruptedException {
        int threadCount = 2000;
        int orderQuantity = 1;
        int poolSize = 200;

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // Kafka 이벤트 처리 대기용 latch 초기화
        kafkaLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    StockCheckReq req = new StockCheckReq();
                    req.setId(productId);
                    req.setQuantity(orderQuantity);

                    // Redis에서 재고 감소
                    stockService.checkAndDecreaseStock(req);

                    // Kafka 이벤트 전송
                   /* Long remainingStock = redisTemplate.opsForValue().get("stock:" + productId);
                    stockDBUpdateProducer.sendStockDBUpdateEvent(productId, remainingStock.intValue());*/

                    successCount.incrementAndGet();
                } catch (OutOfStockException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Redis 감소 완료 대기
        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        boolean completed = kafkaLatch.await(40, TimeUnit.SECONDS);

        Long remainingRedisStock = redisLongTemplate.opsForValue().get("stock:" + productId);
        Item item = itemRepository.findById(productId).get();

// 먼저 로그 찍기
        System.out.println("최종 Redis 재고: " + remainingRedisStock);
        System.out.println("최종 DB 재고: " + item.getStockNumber());
        System.out.println("성공 주문 수: " + successCount.get());
        System.out.println("실패 주문 수: " + failCount.get());

        if (!completed) {
            System.err.println("종료");
        }

// 기존 검증
        assertThat(remainingRedisStock + successCount.get() * orderQuantity).isEqualTo(initialStockNum);
        assertThat((long)item.getStockNumber()).isEqualTo(remainingRedisStock.longValue());
    }
}
