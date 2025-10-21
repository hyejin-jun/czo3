package org.mbc.czo.function.apiChatRoom.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.dto.createChatRoom.CreateChatRoomReq;
import org.mbc.czo.function.apiChatRoom.dto.getChatRoom.GetChatRoomRes;
import org.mbc.czo.function.apiChatRoom.service.ApiChatRoomService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Log4j2
@RestController
@RequestMapping("/api/chatRoom")
@RequiredArgsConstructor
public class ApiChatRoomController {

    private final ApiChatRoomService apiChatRoomService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/")
    public ApiResult<ChatRoom> createRoom(
            @RequestBody CreateChatRoomReq createChatRoomReq,
            Authentication authentication) {
        try {

            ChatRoom newChatRoom = apiChatRoomService.createRoom(createChatRoomReq, authentication);
            //createSuccess
            return ApiResult.created(newChatRoom);

        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // 모든 채팅방 조회
    @GetMapping("/all")
    public ApiResult<List<GetChatRoomRes>> getAllRooms() {

        try{

            List<GetChatRoomRes> rooms = apiChatRoomService.getAllChatRooms();
            return  ApiResult.ok(rooms);

        }catch (Exception e){

            return ApiResult.fail(e.getMessage());
        }
    }

    //내가 참여한 채팅방
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ApiResult<List<GetChatRoomRes>> getMyRooms(Authentication authentication) {
        try{

            List<GetChatRoomRes> rooms = apiChatRoomService.getMyChatRooms(authentication);
            return  ApiResult.ok(rooms);

        }catch (Exception e){

            return ApiResult.fail(e.getMessage());
        }
    }
}
