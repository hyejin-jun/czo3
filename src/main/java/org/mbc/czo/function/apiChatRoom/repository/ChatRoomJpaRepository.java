package org.mbc.czo.function.apiChatRoom.repository;

import org.mbc.czo.function.apiChatRoom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT cr
        FROM ChatRoom cr
        JOIN cr.participants p
        WHERE p.member.mid = :userId
    """)
    List<ChatRoom> findRoomsByParticipant(@Param("userId") String userId);
}
