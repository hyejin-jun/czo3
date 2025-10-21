package org.mbc.czo.function.apiChatRoom.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.domain.ChatMessage;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.dto.chatImageDBUpdateEvent.ChatImageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.repository.ChatMessageJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.apiUploadImage.domain.ChatRoomImages;
import org.mbc.czo.function.apiUploadImage.repository.ChatRoomImageJpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
/*@Component*/
@Service
@RequiredArgsConstructor
public class ChatImageDBUpdateProducer {

    //private final KafkaTemplate<String, ChatImageDBUpdateEvent> chatImageDBUpdateKafkaTemplate;

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ChatMessageJpaRepository chatMessageJpaRepository;

    //카프카 비동기 이벤트 사용 안함
    @Transactional
    public void sendChatImageDBUpdateEvent(ChatImageDBUpdateEvent event) {

        try{

            ChatRoom room = chatRoomJpaRepository.findById(event.getChatRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

            Member sender = memberJpaRepository.findById(event.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("회원 없음"));


            ChatMessage newMessage =
                    ChatMessage.createChatMessage(room, sender, event.getContent(), event.getStatus());

            // 이미지 객체 생성
            ChatRoomImages chatRoomImages = new ChatRoomImages(
                    event.getOriginalName(),
                    event.getStoredName(),
                    event.getRelativePath(),
                    newMessage
            );

            // 연관관계 설정
            newMessage.getImages().add(chatRoomImages);

            // 한 번만 save 하면 message + image 둘 다 저장됨 (cascade = CascadeType.ALL)
            chatMessageJpaRepository.save(newMessage);

        }
        catch(Exception e){

            log.error("sendChatImageDBUpdateEvent 실패", e);
        }
       /* try {

            String topic = "chatImage-DBUpdate-events";
            chatImageDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
            log.info("sendChatImageDBUpdateEvent.이벤트 성공: {}", event);

        } catch (Exception e) {

            log.error("sendChatImageDBUpdateEvent.Kafka 전송 실패, DLQ에 저장", e);
            // DLQ 토픽에 전송 (이벤트 전송 실패 시)
            String topic = "chatImage-DBUpdate-events-dlq";
            chatImageDBUpdateKafkaTemplate.send(
                    topic, String.valueOf(event.getChatRoomId()), event);
        }*/

    }
}
