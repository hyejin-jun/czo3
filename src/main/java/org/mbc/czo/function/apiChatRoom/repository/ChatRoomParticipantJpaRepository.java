package org.mbc.czo.function.apiChatRoom.repository;

import org.mbc.czo.function.apiChatRoom.domain.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantJpaRepository extends JpaRepository<ChatRoomParticipant, Long> {

    // 채팅방 ID와 회원 ID로 참가자 조회
    Optional<ChatRoomParticipant> findByChatRoomIdAndMemberMid(Long chatRoomId, String mid);

   /* // 같은 멤버의 모든 참가 기록 조회
    List<ChatRoomParticipant> findAllByChatRoomIdAndMemberMid(Long chatRoomId, String memberMid);*/

    // 특정 방에 참여중인 모든 참가자 조회
    @Query("SELECT p FROM ChatRoomParticipant p WHERE p.chatRoom.id = :chatRoomId")
    List<ChatRoomParticipant> findParticipantsByRoomId(@Param("chatRoomId") Long chatRoomId);

}
