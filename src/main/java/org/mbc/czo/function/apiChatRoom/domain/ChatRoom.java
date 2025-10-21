package org.mbc.czo.function.apiChatRoom.domain;

import jakarta.persistence.*;
import lombok.*;
import org.mbc.czo.function.apiChatRoom.constant.ChatRoomType;
import org.mbc.czo.function.apiChatRoom.dto.createChatRoom.CreateChatRoomReq;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 그룹 채팅방 이름 (1:1 채팅은 NULL 가능)

    @Enumerated(EnumType.STRING)
    private ChatRoomType type; // PRIVATE or GROUP

    private LocalDateTime createdAt = LocalDateTime.now();

    // ChatRoomParticipant 연관관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomParticipant> participants;

    // ChatMessage 연관관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    public static ChatRoom createChatRoom(CreateChatRoomReq createChatRoomReq) {
        return ChatRoom.builder()
                .name(createChatRoomReq.getName())
                .type(createChatRoomReq.getType())
                .build();
    }
}

