package org.mbc.czo.function.apiStockManagement.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiStockManagement.dto.stockReissedEvent.StockReissueEvent;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class StockReissueConsumer {

    private final ItemRepository itemRepository;
    private final KafkaTemplate<String, StockReissueEvent> stockReissueKafkaTemplate;

    private static final int MAX_KAFKA_EVENT_RETRIES = 10;

    @KafkaListener(
            topics = "stock-reissue-events",
            groupId = "stockReissue",
            containerFactory = "stockReissueListenerFactory")
    public void handleStockReissueEvent(StockReissueEvent event) {

        log.info("재고 재발행 이벤트 수신: {}", event);

    }

    @KafkaListener(
            topics = "stock-reissue-events-dlq",
            groupId = "stockReissue-dlq",
            containerFactory = "stockReissueDLQListenerFactory")
    public void handleStockReissueDLQEvent(StockReissueEvent event) {

        // 재시도 횟수 증가
        event.setRetryCount(event.getRetryCount() + 1);

        // 최대 재시도 체크
        if (event.getRetryCount() > MAX_KAFKA_EVENT_RETRIES) {
            log.error("재시도 최대치 도달, 관리자 확인 필요, 이벤트: {}", event);
            return;
        }

        try {
            stockReissueKafkaTemplate.send("stock-reissue-events", event).get();
            log.info("재처리 성공, 이벤트: {}", event);

        } catch (Exception e) {
            log.error("재처리 실패, 다시 DLQ에 남김", e);

            // 다시 DLQ로 보내기
            stockReissueKafkaTemplate.send("stock-reissue-events-dlq", event);
        }
    }

}
