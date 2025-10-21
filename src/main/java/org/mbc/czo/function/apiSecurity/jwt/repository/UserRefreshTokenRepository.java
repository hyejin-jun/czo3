package org.mbc.czo.function.apiSecurity.jwt.repository;

import jakarta.persistence.LockModeType;
import org.mbc.czo.function.apiSecurity.jwt.domain.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    // 토큰 ID로 조회 + DB row lock
    // DB row lock을 걸어서 조회한다는 의미 DB row lock을 걸어 동시 트랜잭션 충돌 방지
    // 동시 요청이 들어와도 같은 RefreshToken이 중복으로 사용되는 것을 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM UserRefreshToken t WHERE t.tokenId = :tokenId")
    Optional<UserRefreshToken> findByTokenIdForUpdate(@Param("tokenId") String tokenId);

    // 회원 ID(mid)로 모든 RefreshToken 삭제
    //삭제된 row 수를 반환
    int deleteByMember_Mid(String mid);
}

