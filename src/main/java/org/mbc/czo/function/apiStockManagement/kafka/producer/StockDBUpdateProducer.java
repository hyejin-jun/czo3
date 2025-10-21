package org.mbc.czo.function.apiStockManagement.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiStockManagement.dto.stockDBUpdateEvent.StockDBUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class StockDBUpdateProducer {

    private final KafkaTemplate<String, StockDBUpdateEvent> stockDBUpdateKafkaTemplate;

    public void sendStockDBUpdateEvent(Long productId, int remainingStock) {

        //Redis 락을 사용하지 않는 이유
        //중복 이벤트가 발생할 수 있지만 여기선 단순 DB업데이트만 이루어지고 있기 때문에 중복 이벤트 발생해도 괜찮음
        //Redis 락을 사용하지 않으므로써 성능 확보(스레드가 락 대기x)
        StockDBUpdateEvent event =
                new StockDBUpdateEvent(productId, remainingStock, 0);

        try {
            stockDBUpdateKafkaTemplate.send("stock-DBUpdate-events", productId.toString(), event);
            log.info("sendStockDBUpdateEvent.이벤트 성공: {}", event);

        } catch (Exception e) {
            log.error("sendStockDBUpdateEvent.Kafka 전송 실패, DLQ에 저장", e);
            // DLQ 토픽에 전송 (이벤트 전송 실패 시)
            stockDBUpdateKafkaTemplate.send("stock-DBUpdate-events-dlq", event);
        }

    }
}
