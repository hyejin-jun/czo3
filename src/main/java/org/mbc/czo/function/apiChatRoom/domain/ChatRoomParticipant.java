package org.mbc.czo.function.apiChatRoom.domain;

import jakarta.persistence.*;
import lombok.*;
import org.mbc.czo.function.apiMember.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_participant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime leftAt;

    private Long lastReadMessageId; // 읽음 상태 추적

    public static ChatRoomParticipant createChatRoomParticipant(ChatRoom chatRoomParam, Member memberParam) {
        return ChatRoomParticipant.builder()
                .chatRoom(chatRoomParam)
                .member(memberParam)
                .build();
    }
}
