package org.mbc.czo.function.apiChatRoom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoomParticipant;
import org.mbc.czo.function.apiChatRoom.dto.createChatRoom.CreateChatRoomReq;
import org.mbc.czo.function.apiChatRoom.dto.getChatRoom.GetChatRoomRes;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomParticipantJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service("ApiChatRoomService")
@RequiredArgsConstructor
public class ApiChatRoomService {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ChatRoomParticipantJpaRepository chatRoomParticipantJpaRepository;

    @Transactional
    public ChatRoom createRoom(CreateChatRoomReq createChatRoomReq, Authentication authentication) {

        Member member = memberJpaRepository.findById(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        ChatRoom room = ChatRoom.createChatRoom(createChatRoomReq);

        chatRoomJpaRepository.save(room);

        ChatRoomParticipant participant =
                ChatRoomParticipant.createChatRoomParticipant(room, member);

        chatRoomParticipantJpaRepository.save(participant);

        return room;
    }

    // 모든 채팅방 조회
    @Transactional
    public List<GetChatRoomRes> getAllChatRooms() {
        return chatRoomJpaRepository.findAll()
                .stream()
                .map(room -> GetChatRoomRes.createGetAllChatRoomRes(
                        room.getId(),
                        room.getName(),
                        room.getType().name(),
                        chatRoomParticipantJpaRepository
                                .findParticipantsByRoomId(room.getId()).size()
                ))
                .collect(Collectors.toList()); // ← collect로 리스트로 변환
    }

    //내가 참여한 채팅방
    @Transactional
    public List<GetChatRoomRes> getMyChatRooms(Authentication authentication){

        String userId = authentication.getName();

        if(!memberJpaRepository.existsById(authentication.getName())){

            throw new IllegalArgumentException("회원 없음");
        }

        return chatRoomJpaRepository.findRoomsByParticipant(userId)
                .stream()
                .map(room -> GetChatRoomRes.createGetAllChatRoomRes(
                        room.getId(),
                        room.getName(),
                        room.getType().name(),
                        chatRoomParticipantJpaRepository
                                .findParticipantsByRoomId(room.getId()).size()
                ))
                .collect(Collectors.toList());
    }
}
