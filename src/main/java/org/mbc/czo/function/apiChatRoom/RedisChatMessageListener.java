package org.mbc.czo.function.apiChatRoom;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomMemberProfile.ChatRoomMemberProfileRes;
import org.mbc.czo.function.apiChatRoom.service.ApiChatRoomParticipantService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;


//Redis Pub/Sub
//서로 다른 서버간의 브로드캐스트가능
@Log4j2
@Component
@RequiredArgsConstructor
public class RedisChatMessageListener implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ApiChatRoomParticipantService apiChatRoomParticipantService;

    private final ObjectMapper objectMapper; // Jackson ObjectMapper 주입

    //Redis에서 발행된 메시지가 들어오면 호출됨
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 발행된 메시지를 ChatMessagePayloadReq 객체로 역직렬화
            ChatMessagePayloadReq chatMessage = objectMapper.readValue(message.getBody(), ChatMessagePayloadReq.class);

            Long roomId = chatMessage.getRoomId();
            String senderId = chatMessage.getSenderId();
            String content = chatMessage.getContent();

            // STOMP 구독중인 /topic/chat/{roomId}로 브로드캐스트
            simpMessagingTemplate.convertAndSend("/topic/chat-room-" + roomId, chatMessage);

            // 입장/퇴장 시 참가자 목록 전송
            // DB 또는 Redis에서 참가자 프로필 리스트 조회
            Set<ChatRoomMemberProfileRes> participants = apiChatRoomParticipantService.getParticipants(roomId);

            simpMessagingTemplate.convertAndSend(
                    "/topic/chat-room-" + roomId + "/participants",
                    participants
            );
        } catch (Exception e) {
            log.error("RedisChatMessageListener.onMessageError", e);
        }

    }
}
