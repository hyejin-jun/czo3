package org.mbc.czo.function.apiChatRoom.domain;

import jakarta.persistence.*;
import lombok.*;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiUploadImage.domain.ChatRoomImages;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ChatMessageStatus status = ChatMessageStatus.SENT; // 메시지 상태

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoomImages> images = new ArrayList<>();

    public static ChatMessage createChatMessage(
            ChatRoom chatRoomParam, Member senderParam, String contentParam, ChatMessageStatus statusParam) {
        return ChatMessage.builder()
                .chatRoom(chatRoomParam)
                .sender(senderParam)
                .content(contentParam)
                .status(statusParam)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
