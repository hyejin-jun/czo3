package org.mbc.czo.function.apiChatRoom.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.RedisChatMessagePublisher;
import org.mbc.czo.function.apiChatRoom.domain.ChatMessage;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.repository.ChatMessageJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatMessageDBUpdateConsumer {

    private final KafkaTemplate<String, ChatMessageDBUpdateEvent> chatMessageDBUpdateKafkaTemplate;

    private static final int MAX_KAFKA_EVENT_RETRIES = 10;

    private final ChatMessageJpaRepository chatMessageJpaRepository;
    private final RedisChatMessagePublisher redisChatMessagePublisher; // 서버 브로드캐스트용, 필요 시 사용
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @KafkaListener(
            topics = "chatMessage-DBUpdate-events",
            containerFactory = "chatMessageDBUpdateListenerFactory",
            groupId = "chatMessageDBUpdate"
    )
    public void consume(ChatMessageDBUpdateEvent event) {

        try {

            ChatRoom room = chatRoomJpaRepository.findById(event.getChatRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

            Member sender = memberJpaRepository.findById(event.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

            ChatMessage newMessage =
                    ChatMessage.createChatMessage(room, sender, event.getContent(), event.getStatus());

            //DB 저장
            chatMessageJpaRepository.save(newMessage);
            log.info("consume. DB 저장 완료: {}", newMessage);

            // Redis에 발행 -> 구독 중인 모든 서버에 전달됨
           /* redisChatMessagePublisher.publish(
                    new ChatMessagePayloadReq(
                            event.getStatus(),
                            event.getChatRoomId(),
                            event.getSenderId(),
                            event.getContent(),
                            event.getImageUrls(),
                            newMessage.getCreatedAt()));*/


        } catch (Exception e) {
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
            topics = "chatMessage-DBUpdate-events-dlq",
            containerFactory = "chatMessageDBUpdateDLQListenerFactory",
            groupId = "chatMessageDBUpdate-dlq"
    )
    public void consumeDLQ(ChatMessageDBUpdateEvent event) {

        // 재시도 횟수 증가
        event.setRetryCount(event.getRetryCount() + 1);

        // 최대 재시도 체크
        if (event.getRetryCount() > MAX_KAFKA_EVENT_RETRIES) {
            log.error("재시도 최대치 도달, 관리자 확인 필요, 이벤트: {}", event);
            return;
        }

        try {
            String topic = "chatMessage-DBUpdate-events";
            chatMessageDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
            log.info("재처리 성공, 이벤트: {}", event);
        } catch (Exception e) {
            log.error("재처리 실패, 다시 DLQ에 남김", e);

            sendToDLQ(event);
        }
    }

    private void sendToDLQ(ChatMessageDBUpdateEvent event) {
        String topic = "chatMessage-DBUpdate-events-dlq";
        chatMessageDBUpdateKafkaTemplate.send(
                topic, String.valueOf(event.getChatRoomId()), event);
    }
}
