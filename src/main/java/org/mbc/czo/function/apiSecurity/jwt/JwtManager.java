package org.mbc.czo.function.apiSecurity.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginRes;
import org.mbc.czo.function.apiSecurity.jwt.exception.InvalidTokenException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Log4j2
@Component
public class JwtManager {

    private final SecretKey accessKey;
    private final long accessValidityMs;

    @Getter
    private final SecretKey refreshKey;
    @Getter
    private final long refreshValidityMs;

    public JwtManager(JwtProperties props) {
        this.accessKey = Keys.hmacShaKeyFor(props.getAccessSecret().getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = props.getAccessValidityMs();

        this.refreshKey = Keys.hmacShaKeyFor(props.getRefreshSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshValidityMs = props.getRefreshValidityMs();

    }

    // ================= Access Token =================
    //만료 시간과 발급 시간을 포함한 Access Token을 생성
    //signWith(accessKey, HS256) → 토큰 서명을 통해 변조 방지.
    public String generateAccessToken(String userId, int version, Set<String> roles) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValidityMs);

        return buildToken(userId, version, now, expiry, roles, accessKey, "access");
    }

    //전달받은 Access Token이 유효한지 확인.
    //검증이 통과하면 JWT 내부의 Claims(주체, 발급 시간, 만료 시간 등) 반환.
    public Claims validateAccessToken(String token) {

        return validateToken(token, accessKey, "access");
    }
    // ================= Refresh Token =================
    //로그인 세션이 만료되었을 때 새로운 Access Token을 발급할 목적으로 사용.
    public String generateRefreshToken(String userId, int version, LocalDateTime firstIssuedAt) {

        Date now = new Date();
        // 토큰 발급 시점
        Date expiry = Date.from(firstIssuedAt.atZone(ZoneId.systemDefault()).toInstant()
                .plusMillis(refreshValidityMs));

        return buildToken(userId, version, now, expiry, null, refreshKey, "refresh");
    }


    //Refresh Token의 서명과 만료 시간 검증.
    //유효하면 내부 Claims 반환.
    public Claims validateRefreshToken(String token) {

        return validateToken(token, refreshKey, "refresh");
    }


    // ================= Token Refresh =================

    //Refresh Token이 존재하면 새로운 Access Token을 발급한다.
  /*  public String refreshAccessToken(String refreshToken) {
        Claims claims = validateRefreshToken(refreshToken);
        String userId = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);  // Access Token에 role 포함 시
        return generateAccessToken(userId, new HashSet<>(roles));
    }*/


    // ================= Common Methods =================
    private String buildToken(String userId, int version, Date now, Date expiry, Set<String> roles, SecretKey key, String type) {


        String tokenId = UUID.randomUUID().toString();

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setId(tokenId)
                .claim("typ", type)
                .claim("version", version)
                .signWith(key, SignatureAlgorithm.HS256);


        if (roles != null && !roles.isEmpty()) {
            builder.claim("roles", roles);
        }

        return builder.compact();
    }

    private Claims validateToken(String token, SecretKey key, String type) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired " + type + " token", e);
        }
    }

    // ================= Token generate =================
    public MemberLoginRes generateTokens(String userId, int version, Set<String> roles, LocalDateTime firstIssuedAt) {

        return MemberLoginRes.createMemberLoginRes(
                generateAccessToken(userId,version, roles),
                generateRefreshToken(userId, version, firstIssuedAt));
    }
}
