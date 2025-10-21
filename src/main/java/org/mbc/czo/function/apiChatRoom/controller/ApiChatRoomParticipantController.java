package org.mbc.czo.function.apiChatRoom.controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomMemberProfile.ChatRoomMemberProfileRes;
import org.mbc.czo.function.apiChatRoom.service.ApiChatRoomParticipantService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/chatRoomParticipant")
@RequiredArgsConstructor
public class ApiChatRoomParticipantController {

    private final ApiChatRoomParticipantService apiChatRoomParticipantService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{roomId}")
    public ApiResult<Void> joinRoom(
            @PathVariable Long roomId,
            Authentication authentication) {

        try {
            apiChatRoomParticipantService.addParticipant(roomId, authentication);
            return ApiResult.none();

        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    @GetMapping("/{roomId}")
    public ApiResult<Set<ChatRoomMemberProfileRes>> getParticipants(@PathVariable Long roomId) {

        try {
            Set<ChatRoomMemberProfileRes> participants = apiChatRoomParticipantService.getParticipants(roomId);
            return ApiResult.ok(participants);

        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{roomId}")
    public ApiResult<String> leaveRoom(
            @PathVariable Long roomId,
            Authentication authentication) {

        try {
            apiChatRoomParticipantService.leaveParticipant(roomId, authentication);
            return ApiResult.none();

        } catch (Exception e) {

            return ApiResult.fail(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{roomId}")
    public ApiResult<String> exitRoom(
            @PathVariable Long roomId,
            Authentication authentication) {

        try {
            apiChatRoomParticipantService.exitParticipant(roomId, authentication);
            return ApiResult.none();

        } catch (Exception e) {

            return ApiResult.fail(e.getMessage());
        }
    }

}
