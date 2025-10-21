package org.mbc.czo.function.apiChatRoom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.mbc.czo.function.apiChatRoom.dto.createChatMessage.CreateChatMessageReq;
import org.mbc.czo.function.apiChatRoom.service.ApiChatMessageService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ApiChatMessageController {

    private final ApiChatMessageService apiChatMessageService;

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat.send.{roomId}")
    //@DestinationVariable은 STOMP/WebSocket에서 URL 경로 변수를 받는 어노테이션
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload CreateChatMessageReq createChatMessageReq,
            Authentication authentication) {

        try{
            //MessageMapping은 @Controller사용해야 하므로 apiResult사용불가
            // DB 저장
            apiChatMessageService.saveMessage(roomId, createChatMessageReq, authentication, ChatMessageStatus.SENT);
        }
        catch (Exception e){

            log.error(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/chatMessage/{roomId}")
    @ResponseBody
    public ApiResult<List<ChatMessagePayloadReq>> getMessages(@PathVariable Long roomId) {
        try {
            List<ChatMessagePayloadReq> messages = apiChatMessageService.getMessages(roomId);

            return ApiResult.ok(messages);

        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }
}
