package org.mbc.czo.function.apiStockManagement.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiStockManagement.dto.stockReissedEvent.StockReissueEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class StockReissueProducer {

    private final KafkaTemplate<String, StockReissueEvent> stockReissueKafkaTemplate;


    public void sendStockReissueEvent(Long productId, int remainingStock, int reissuThreshold) {

        StockReissueEvent event = new StockReissueEvent(productId, remainingStock, reissuThreshold, 0);

        try {
            stockReissueKafkaTemplate.send("stock-reissue-events", event);
            log.info("재발주 이벤트 발송 성공: productId={}", productId);

        } catch (Exception e) {
            log.error("sendReissueEvent.Kafka 전송 실패, DLQ에 저장", e);

            stockReissueKafkaTemplate.send("stock-reissue-events-dlq", event);
        }
    }
}
