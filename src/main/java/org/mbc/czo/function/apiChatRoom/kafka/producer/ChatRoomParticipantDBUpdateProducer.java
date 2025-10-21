package org.mbc.czo.function.apiChatRoom.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoomParticipant;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomParticipantDBUpdateEvent.ChatRoomParticipantDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomParticipantJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
/*@Component*/
@Service
@RequiredArgsConstructor
public class ChatRoomParticipantDBUpdateProducer {

    //private final KafkaTemplate<String, ChatRoomParticipantDBUpdateEvent> chatRoomParticipantDBUpdateKafkaTemplate;

    private final ChatRoomParticipantJpaRepository chatRoomParticipantJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    //카프카 비동기 이벤트 사용 안함
    @Transactional
    public void sendChatRoomParticipantDBUpdateEvent(ChatRoomParticipantDBUpdateEvent event) {

        try{

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


        }
        catch (Exception e){
            log.error("sendChatRoomParticipantDBUpdateEvent 실패,: {}", event, e);
        }
       /* try {

            String topic = "chatRoomParticipant-DBUpdate-events";
            chatRoomParticipantDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
            log.info("ChatRoomParticipant DB Update 이벤트 전송 성공: {}", event);

        } catch (Exception e) {

            log.error("Kafka 전송 실패, DLQ에 저장: {}", event, e);
            String topic = "chatRoomParticipant-DBUpdate-events-dlq";
            chatRoomParticipantDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);;
        }*/
    }
}
