package org.mbc.czo.function.apiChatRoom.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.RedisChatMessagePublisher;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoomParticipant;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomParticipantDBUpdateEvent.ChatRoomParticipantDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomParticipantJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatRoomParticipantDBUpdateConsumer {

    private final KafkaTemplate<String, ChatRoomParticipantDBUpdateEvent> chatRoomParticipantDBUpdateKafkaTemplate;
    private final ChatRoomParticipantJpaRepository chatRoomParticipantJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    private final RedisChatMessagePublisher redisChatMessagePublisher; // 서버 브로드캐스트용, 필요 시 사용

    private static final int MAX_KAFKA_EVENT_RETRIES = 10;

    // 일반 Kafka Consumer
    @KafkaListener(
            topics = "chatRoomParticipant-DBUpdate-events",
            containerFactory = "chatRoomParticipantDBUpdateListenerFactory",
            groupId = "chatRoomParticipantDBUpdate"
    )
    public void consume(ChatRoomParticipantDBUpdateEvent event) {
        try {
            Member member = memberJpaRepository.findById(event.getParticipantId())
                    .orElseThrow(() -> new IllegalArgumentException("consume: Member not found"));

            ChatRoom chatRoom =  chatRoomJpaRepository.findById(event.getChatRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("consume: roomId not found"));

            switch (event.getEventType()) {

                case FIRSTJOIN -> {

                    // 신규 참가자 기록 생성
                    chatRoomParticipantJpaRepository.save(
                            ChatRoomParticipant.createChatRoomParticipant(chatRoom, member));

                }

                case JOIN -> {

                    // 기존 참가자 조회
                    ChatRoomParticipant participant =
                            chatRoomParticipantJpaRepository.findByChatRoomIdAndMemberMid(
                                    chatRoom.getId(), member.getMid())
                                    .orElseThrow(() -> new IllegalArgumentException("consume: participant not found"));

                    participant.setLeftAt(null); // 다시 참여로 표시
                    participant.setJoinedAt(LocalDateTime.now());
                    chatRoomParticipantJpaRepository.save(participant);

                }
                case LEAVE -> {

                    Optional<ChatRoomParticipant> participant =
                            chatRoomParticipantJpaRepository.findByChatRoomIdAndMemberMid(
                                    event.getChatRoomId(),
                                    event.getParticipantId());

                    //Optional 안에 값이 있으면 실행
                    participant.ifPresent(p -> {
                        p.setLeftAt(LocalDateTime.now());
                        chatRoomParticipantJpaRepository.save(p);
                    });
                }

                case EXIT -> {

                    // DB에서 참가자 삭제
                    ChatRoomParticipant participant =
                            chatRoomParticipantJpaRepository.findByChatRoomIdAndMemberMid(
                                    chatRoom.getId(), member.getMid()).
                                    orElseThrow(() -> new IllegalArgumentException("consume: Member not found in Room"));

                    chatRoomParticipantJpaRepository.delete(participant);

                    /*redisChatMessagePublisher.publish(
                            new ChatMessagePayloadReq(
                                    ChatMessageStatus.EXIT,
                                    event.getChatRoomId(),
                                    event.getParticipantId(),
                                    event.getParticipantId() + " 님이 퇴장하셧습니다."));*/
                }
                case BREAKROOM -> {

                    // DB에서 참가자 삭제
                    List<ChatRoomParticipant> participants =
                            chatRoomParticipantJpaRepository.findParticipantsByRoomId(event.getChatRoomId());
                    chatRoomParticipantJpaRepository.deleteAll(participants);

                    // 채팅방 삭제
                    chatRoomJpaRepository.delete(chatRoom);
                }

                default -> throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + event.getEventType());
            }

            log.info("ChatRoomParticipant DB 저장 완료: {}", event);

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

    // DLQ Consumer
    @KafkaListener(
            topics = "chatRoomParticipant-DBUpdate-events-dlq",
            containerFactory = "chatRoomParticipantDBUpdateDLQListenerFactory",
            groupId = "chatRoomParticipantDBUpdate-dlq"
    )
    public void consumeDLQ(ChatRoomParticipantDBUpdateEvent event) {

        // 재시도 횟수 증가
        event.setRetryCount(event.getRetryCount() + 1);

        // 최대 재시도 체크
        if (event.getRetryCount() > MAX_KAFKA_EVENT_RETRIES) {
            log.error("재시도 최대치 도달, 관리자 확인 필요, 이벤트: {}", event);
            return;
        }

        try {

            String topic = "chatRoomParticipant-DBUpdate-events";
            chatRoomParticipantDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);;
            log.info("재처리 성공, 이벤트: {}", event);

        } catch (Exception e) {
            log.error("재처리 실패, 다시 DLQ에 남김", e);

            sendToDLQ(event);
        }
    }

    private void sendToDLQ(ChatRoomParticipantDBUpdateEvent event) {
        String topic = "chatRoomParticipant-DBUpdate-events-dlq";
        chatRoomParticipantDBUpdateKafkaTemplate.send(
                topic, String.valueOf(event.getChatRoomId()), event);;
    }
}
