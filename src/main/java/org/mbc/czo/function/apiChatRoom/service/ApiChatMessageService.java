package org.mbc.czo.function.apiChatRoom.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiChatRoom.domain.ChatMessage;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatMessagePayload.ChatMessagePayloadReq;
import org.mbc.czo.function.apiChatRoom.dto.createChatMessage.CreateChatMessageReq;
import org.mbc.czo.function.apiChatRoom.kafka.producer.ChatMessageDBUpdateProducer;
import org.mbc.czo.function.apiChatRoom.repository.ChatMessageJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;

import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service("ApiChatMessageService")
@RequiredArgsConstructor
public class ApiChatMessageService {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ChatMessageJpaRepository chatMessageJpaRepository;

    private final ChatMessageDBUpdateProducer chatMessageDBUpdateProducer;

    @Transactional
    public void saveMessage(Long roomId, CreateChatMessageReq createChatMessageReq, Authentication authentication, ChatMessageStatus chatMessageStatus) throws JsonProcessingException {

        String userId = authentication.getName();

        ChatRoom room = chatRoomJpaRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        Member sender = memberJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // DB 저장
        ChatMessage newMessage = ChatMessage.createChatMessage(
                room, sender, createChatMessageReq.getContent(), chatMessageStatus);
        chatMessageJpaRepository.save(newMessage);

        // Redis 발행
        ChatMessageDBUpdateEvent event = new ChatMessageDBUpdateEvent(
                chatMessageStatus,
                roomId,
                userId,
                createChatMessageReq.getContent(),
                newMessage.getCreatedAt(),
                createChatMessageReq.getImageUrls(),
               0
        );

        chatMessageDBUpdateProducer.sendChatMessageDBUpdateEvent(event);
    }

    @Transactional
    public List<ChatMessagePayloadReq> getMessages(Long roomId) {
        return chatMessageJpaRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(m -> {
                    List<String> imageUrls = m.getImages().stream()
                            .map(img -> "/uploads/chatroom/" + img.getUploadPath())
                            .collect(Collectors.toList());

                    //이미지는 메세지 하나에 한장만
                    return new ChatMessagePayloadReq(
                            m.getStatus(),
                            roomId,
                            m.getSender().getMid(),
                            m.getContent() != null ? m.getContent() : "",
                            imageUrls.isEmpty()?null:imageUrls.getFirst(),
                            m.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

}
