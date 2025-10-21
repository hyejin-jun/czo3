package org.mbc.czo.function.apiChatRoom.kafka.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.mbc.czo.function.apiChatRoom.RedisChatMessagePublisher;
import org.mbc.czo.function.apiChatRoom.domain.ChatMessage;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;

import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.mbc.czo.function.apiMember.domain.Member;

import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ChatMessageDBUpdateProducer {

    // private final KafkaTemplate<String, ChatMessageDBUpdateEvent> chatMessageDBUpdateKafkaTemplate;
    private final RedisChatMessagePublisher redisChatMessagePublisher;

    //카프카 비동기 이벤트 사용 안함
    public void sendChatMessageDBUpdateEvent(ChatMessageDBUpdateEvent event) throws JsonProcessingException {

        int publishCnt = ( event.getImageUrls() == null || event.getImageUrls().isEmpty()) ? 1 :  event.getImageUrls().size();
        log.info("publishCnt:{}", publishCnt);

        for (int i = 0; i < publishCnt; i++) {

            redisChatMessagePublisher.publish(
                    new ChatMessagePayloadReq(
                            event.getStatus(),
                            event.getChatRoomId(),
                            event.getSenderId(),
                            (i == 0) ? event.getContent() : "",
                            (event.getImageUrls() == null || event.getImageUrls().isEmpty()) ? null :  event.getImageUrls().get(i),
                            event.getCreatedAt()
                    )
            );
        }
        // Redis publish

      /*  try {

            String topic = "chatMessage-DBUpdate-events";
            chatMessageDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
            log.info("sendChatMessageUpdateEvent.이벤트 성공: {}", event);

        } catch (Exception e) {

            log.error("sendChatMessageUpdateEvent.Kafka 전송 실패, DLQ에 저장", e);
            // DLQ 토픽에 전송 (이벤트 전송 실패 시)
            String topic = "chatMessage-DBUpdate-events-dlq";
            chatMessageDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
        }*/

    }
}
