package org.mbc.czo.function.apiStockManagement.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiStockManagement.dto.stockDBUpdateEvent.StockDBUpdateEvent;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class StockDBUpdateConsumer {

    private final ItemRepository itemRepository;
    private final KafkaTemplate<String, StockDBUpdateEvent> stockDBUpdateKafkaTemplate;

    private static final int MAX_KAFKA_EVENT_RETRIES = 10;

    //동시에 10개의 스레드로 병렬 처리
    @Transactional
    @KafkaListener(
            topics = "stock-DBUpdate-events",
            groupId = "stockDBUpdate",
            containerFactory = "stockDBUpdateListenerFactory")
    public void handleStockUpdateEvent(StockDBUpdateEvent event) {

        try{
            log.info("DB 업데이트 이벤트 수신: {}", event);

            itemRepository.updateStockIfLower(event.getId(), event.getRemainStock());
        }
       catch(Exception e){
           log.error("DB 저장 실패, DLQ로 이동: {}", event, e);

           // 재시도 횟수 체크
           if (event.getRetryCount() < MAX_KAFKA_EVENT_RETRIES) {

               event.setRetryCount(event.getRetryCount() + 1);
               sendToDLQ(event);
           } else {
               log.error("재시도 최대치 도달, 관리자 확인 필요, 이벤트: {}", event);
           }
       }
    }

    @KafkaListener(
            topics = "stock-DBUpdate-events-dlq",
            groupId = "stockDBUpdate-dlq",
            containerFactory = "stockDBUpdateDLQListenerFactory")
    public void handleStockUpdateDLQEvent(StockDBUpdateEvent event) {

        // 재시도 횟수 증가
        event.setRetryCount(event.getRetryCount() + 1);

        // 최대 재시도 체크
        if (event.getRetryCount() > MAX_KAFKA_EVENT_RETRIES) {
            log.error("재시도 최대치 도달, 관리자 확인 필요, 이벤트: {}", event);
            return;
        }

        try {
            stockDBUpdateKafkaTemplate.send("stock-DBUpdate-events", event);
            log.info("재처리 성공, 이벤트: {}", event);
        } catch (Exception e) {
            log.error("재처리 실패, 다시 DLQ에 남김", e);

            sendToDLQ(event);
        }
    }

    private void sendToDLQ(StockDBUpdateEvent event) {
        stockDBUpdateKafkaTemplate.send("stock-DBUpdate-events-dlq", event);
    }
}
