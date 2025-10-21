package org.mbc.czo.function.apiChatRoom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiChatRoom.constant.ChatMessageStatus;
import org.mbc.czo.function.apiChatRoom.constant.ChatRoomParticipantEventType;
import org.mbc.czo.function.apiChatRoom.domain.ChatRoomParticipant;
import org.mbc.czo.function.apiChatRoom.dto.chatMessageDBUpdateEvent.ChatMessageDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomMemberProfile.ChatRoomMemberProfileRes;
import org.mbc.czo.function.apiChatRoom.dto.chatRoomParticipantDBUpdateEvent.ChatRoomParticipantDBUpdateEvent;
import org.mbc.czo.function.apiChatRoom.dto.createChatMessage.CreateChatMessageReq;
import org.mbc.czo.function.apiChatRoom.kafka.producer.ChatMessageDBUpdateProducer;
import org.mbc.czo.function.apiChatRoom.kafka.producer.ChatRoomParticipantDBUpdateProducer;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomJpaRepository;
import org.mbc.czo.function.apiChatRoom.repository.ChatRoomParticipantJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.common.redisLock.AtomicOperation;
import org.mbc.czo.function.common.redisLock.RedisAtomicOperationWithLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service("ApiChatRoomParticipantService")
@RequiredArgsConstructor
public class ApiChatRoomParticipantService {

    private final StringRedisTemplate redisTemplate;

    private final ChatRoomParticipantDBUpdateProducer chatRoomParticipantDBUpdateProducer;
    private final ChatMessageDBUpdateProducer chatMessageDBUpdateProducer;

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final MemberJpaRepository memberJpaRepository;
    private final ChatRoomParticipantJpaRepository chatRoomParticipantJpaRepository;

    private final RedisAtomicOperationWithLock redisAtomicOperationWithLock;
    private final ApiChatMessageService apiChatMessageService;

    private static final long LOCK_WAIT_SEC = 3;
    private static final long LOCK_LEASE_SEC = 10;

    //어느 서버에 붙었는지
    @Value("${server.id}")
    private String currentServerId;

    @Transactional
    public void addParticipant(Long roomId, Authentication authentication) {

        String userId = authentication.getName();

        AtomicOperation<Void> operation = () ->{

            // 1. 채팅방 & 회원 체크
            if (!chatRoomJpaRepository.existsById(roomId)) {
                throw new IllegalArgumentException("채팅방 없음");
            }
            if (!memberJpaRepository.existsById(userId)) {
                throw new IllegalArgumentException("회원 없음");
            }

            String participantKey = "chatroom:" + roomId + ":participants";

            // 2. Redis 캐시 초기화 및 DB 복구
            getParticipants(roomId);

            // 3. Redis에 참가자 추가
            Long addedCount = redisTemplate.opsForSet().add(participantKey, userId);
            log.info("addParticipant: {}",addedCount );
            // 4. 유저 세션 생성 + TTL 설정
            String sessionKey = "user:" + userId + ":session";
            redisTemplate.opsForHash().putAll(sessionKey, Map.of(
                    "serverId", currentServerId,
                    "sessionId", UUID.randomUUID().toString(),
                    "lastActive", LocalDateTime.now().toString()
            ));

            //방에서 나갈때 삭제
            //redisTemplate.expire(sessionKey, 30, TimeUnit.MINUTES);

            //최초입장인지 체크
            ChatRoomParticipantEventType eventType = chatRoomParticipantJpaRepository.findByChatRoomIdAndMemberMid(roomId, userId).isPresent()
                    ? ChatRoomParticipantEventType.JOIN : ChatRoomParticipantEventType.FIRSTJOIN;

            if (eventType == ChatRoomParticipantEventType.FIRSTJOIN){
                //입, 퇴장 메시지 DB 업데이트 이벤트
                apiChatMessageService.saveMessage(
                        roomId,
                        new CreateChatMessageReq(userId + " 님이 입장하셧습니다.", null),
                        authentication,
                        ChatMessageStatus.JOIN);
/*                chatMessageDBUpdateProducer.sendChatMessageDBUpdateEvent(
                        new ChatMessageDBUpdateEvent(ChatMessageStatus.JOIN, roomId, userId, , null, 0));*/
            }

            // 5. DB 업데이트 이벤트 생성
            chatRoomParticipantDBUpdateProducer.sendChatRoomParticipantDBUpdateEvent(
                    new ChatRoomParticipantDBUpdateEvent(
                            roomId,
                            userId,
                            eventType,
                            0
                    )
            );
            return null;
        };

        redisAtomicOperationWithLock.executeAtomicOperationWithLock(
                "chatroom:" + roomId + ":lock", LOCK_WAIT_SEC, LOCK_LEASE_SEC, operation
        );

    }


    @Transactional
    public Set<ChatRoomMemberProfileRes> getParticipants(Long roomId) {

        String key = "chatroom:" + roomId + ":participants";

        // Redis에서 참가자 ID 조회
        Set<String> participantIds = redisTemplate.opsForSet().members(key);
        log.info("🔸 Redis participantIds for room {}: {}", roomId, participantIds);
        // 반환할 최종 결과
        Set<ChatRoomMemberProfileRes> result;

        if (participantIds == null || participantIds.isEmpty()) {
            // Redis에 값이 없으면 DB에서 복구
            List<ChatRoomParticipant> dbParticipants = chatRoomParticipantJpaRepository.findParticipantsByRoomId(roomId)
                    .stream()
                    .filter(p -> p.getLeftAt() == null)
                    .toList();
            log.info("🟡 DB에서 복구한 참가자 목록: {}",
                    dbParticipants.stream().map(p -> p.getMember().getMid()).toList());
            // Redis에 MID만 저장
            if (!dbParticipants.isEmpty()) {
                String[] mids = dbParticipants.stream()
                        .map(p -> p.getMember().getMid())
                        .toArray(String[]::new);
                log.info("🟢 Redis에 저장할 mids: {}", Arrays.toString(mids));
                redisTemplate.opsForSet().add(key, mids);
                // redisTemplate.expire(key, 1, TimeUnit.HOURS);
            }

            // 최종 반환값 생성
            result = dbParticipants.stream()
                    .map(p -> {
                        String profilePath = (p.getMember().getProfileImage() != null)
                                ? p.getMember().getProfileImage().getUploadPath()
                                : null; // 없으면 null 처리
                        return ChatRoomMemberProfileRes.createChatRoomMemberProfileRes(
                                p.getMember().getMid(),
                                p.getMember().getMname(),
                                profilePath
                        );
                    })
                    .collect(Collectors.toSet());

        } else {
            // Redis에 있는 MID 기준으로 다시 DB에서 조회 → 최신 프로필 정보 반영
            List<Member> members = memberJpaRepository.findAllById(participantIds);
            result = members.stream()
                    .map(m -> {
                        String profilePath = (m.getProfileImage() != null)
                                ? m.getProfileImage().getUploadPath()
                                : null;
                        return ChatRoomMemberProfileRes.createChatRoomMemberProfileRes(
                                m.getMid(),
                                m.getMname(),
                                profilePath
                        );
                    })
                    .collect(Collectors.toSet());

        }

        log.info("Participant: " + result);


        return result;
    }

    @Transactional
    public void leaveParticipant(Long roomId, Authentication authentication) {

        String userId = authentication.getName();

        AtomicOperation<Void> operation = () ->{

            if(!chatRoomJpaRepository.existsById(roomId)) {
                throw new IllegalArgumentException("채팅방 없음");
            }

            if(!memberJpaRepository.existsById(userId)) {
                throw new IllegalArgumentException("회원 없음");
            }

            String participantKey = "chatroom:" + roomId + ":participants";

            // Redis에서 참가자 제거
            redisTemplate.opsForSet().remove(participantKey, userId);
            redisTemplate.delete("user:" + userId + ":session");

            // 참가자 DB 업데이트 이벤트
            chatRoomParticipantDBUpdateProducer.sendChatRoomParticipantDBUpdateEvent(
                    new ChatRoomParticipantDBUpdateEvent(
                            roomId,
                            userId,
                            ChatRoomParticipantEventType.LEAVE,
                            0
                    ));

            return null;
        };

        redisAtomicOperationWithLock.executeAtomicOperationWithLock(
                "chatroom:" + roomId + ":lock", LOCK_WAIT_SEC, LOCK_LEASE_SEC, operation
        );

    }

    @Transactional
    public void exitParticipant(Long roomId, Authentication authentication){
        String userId = authentication.getName();

        AtomicOperation<Void> operation = () ->{

            String participantKey = "chatroom:" + roomId + ":participants";

            // Redis 참가자 제거
            redisTemplate.opsForSet().remove(participantKey, userId);
            redisTemplate.delete("user:" + userId + ":session");

            // 방의 남은 참가자 수 확인 0명이면 방 제거
            Long remaining = redisTemplate.opsForSet().size(participantKey);
            log.info("removeParticipant: {}",remaining );

            if (remaining == null || remaining == 0) {
                // 마지막 참가자가 나갔으면 방 삭제
                breakChatRoom(roomId);
            }

            //입, 퇴장 메시지 DB 업데이트 이벤트
            apiChatMessageService.saveMessage(
                    roomId,
                    new CreateChatMessageReq(userId + " 님이 퇴장하셧습니다.", null),
                    authentication,
                    ChatMessageStatus.EXIT);
          /*  chatMessageDBUpdateProducer.sendChatMessageDBUpdateEvent(
                    new ChatMessageDBUpdateEvent(ChatMessageStatus.EXIT, roomId, userId, userId + " 님이 퇴장하셧습니다.", null, 0));*/

            // 참가자 DB 업데이트 이벤트
            chatRoomParticipantDBUpdateProducer.sendChatRoomParticipantDBUpdateEvent(
                    new ChatRoomParticipantDBUpdateEvent(
                            roomId,
                            userId,
                            (remaining == null || remaining == 0) ? ChatRoomParticipantEventType.BREAKROOM : ChatRoomParticipantEventType.EXIT,
                            0
                    ));

            return null;
        };
        redisAtomicOperationWithLock.executeAtomicOperationWithLock(
                "chatroom:" + roomId + ":lock", LOCK_WAIT_SEC, LOCK_LEASE_SEC, operation
        );
    }

    @Transactional
    public void breakChatRoom(Long roomId) {
        // 1. 채팅방 조회
        if(!chatRoomJpaRepository.existsById(roomId)){
            throw new IllegalArgumentException("채팅방 없음");
        }

        // 4. Redis 참가자 Set 삭제
        String participantKey = "chatroom:" + roomId + ":participants";
        Set<String> participantIds = redisTemplate.opsForSet().members(participantKey);
        if (participantIds != null && !participantIds.isEmpty()) {
            // 5. 각 유저 세션 삭제
            for (String userId : participantIds) {
                String sessionKey = "user:" + userId + ":session";
                redisTemplate.delete(sessionKey);
            }
        }
        redisTemplate.delete(participantKey);

        log.info("ChatRoom [{}] 삭제 완료, Redis 캐시 및 세션 제거 완료", roomId);
    }
}
