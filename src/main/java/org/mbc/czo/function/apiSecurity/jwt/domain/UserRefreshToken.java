package org.mbc.czo.function.apiSecurity.jwt.domain;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.*;
import lombok.*;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiSecurity.jwt.RefreshTokenHasher;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 토큰 소유자

    @Column(nullable = false, unique = true)
    private String tokenId; // JWT jti(jwtID) 대신 독자적 ID

    @Column(nullable = false)
    private String hashedToken; // 해시 형태로 저장

    // 만료일
    @Column(nullable = false)
    private LocalDateTime expiresDate;

    // 교체된 날짜 (null 이면 아직 유효)
    private LocalDateTime replacedDate;

    //로그인 시 시간 (만료시간 고정에 필요)
    private LocalDateTime firstIssuedAt;

    @Column(nullable = false)
    private Integer tokenVersion = 0; // 토큰 단위 버전 관리

    // ===== 생성 메서드 =====
    public static UserRefreshToken createToken(
            Member member, String newToken, LocalDateTime firstIssuedAt, long refreshValidityMs,
            RefreshTokenHasher hasher, Integer version, SecretKey refreshKey) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(newToken)
                .getBody();

        UserRefreshToken createdToken = new UserRefreshToken();

        createdToken.tokenId = claims.getId();
        createdToken.hashedToken = hasher.hash(newToken);

        // LocalDateTime → ZonedDateTime → Instant → plusMillis → LocalDateTime
        ZoneId zoneId = ZoneId.systemDefault();
        createdToken.expiresDate = firstIssuedAt.atZone(zoneId)   // LocalDateTime → ZonedDateTime
                .toInstant()                                      // ZonedDateTime → Instant
                .plusMillis(refreshValidityMs)                   // 만료 시간 추가
                .atZone(zoneId)                                  // Instant → ZonedDateTime
                .toLocalDateTime();

        /*createdToken.expiresDate = claims.getExpiration()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();*/
        createdToken.firstIssuedAt = firstIssuedAt;
        createdToken.member = member;
        createdToken.tokenVersion = version;
        return createdToken;
    }

    // ===== 회전 =====
    public void rotate() {
        //지금 이 토큰이 교체되었음을 기록
        this.replacedDate = LocalDateTime.now();
        //회전한 이후에도 이전 버전의 토큰은 무효화
        //this.tokenVersion += 1; // 회전 시 버전 업
    }
}

