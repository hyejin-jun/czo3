package org.mbc.czo.function.apiChatRoom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class RedisChatMessagePublisher {

    private final RedisTemplate<String, ChatMessagePayloadReq> chatRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessagePayloadReq chatMessagePayloadReq) throws JsonProcessingException {

        String channel = "chat-room-" + chatMessagePayloadReq.getRoomId();
        chatRedisTemplate.convertAndSend(channel, chatMessagePayloadReq);

        // 새 ObjectMapper 사용 금지! 스프링에서 주입받은 ObjectMapper 사용
        String json = objectMapper.writeValueAsString(chatMessagePayloadReq);
        log.info("Redis 채널 발행: {}, message: {}, json: {}", channel, chatMessagePayloadReq, json);
    }
}
