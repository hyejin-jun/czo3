package org.mbc.czo.function.apiSecurity.jwt.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.apiSecurity.jwt.JwtManager;
import org.mbc.czo.function.apiSecurity.jwt.RefreshTokenHasher;
import org.mbc.czo.function.apiSecurity.jwt.domain.UserRefreshToken;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginReq;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginRes;
import org.mbc.czo.function.apiSecurity.jwt.dto.refreshToken.RefreshTokenRes;
import org.mbc.czo.function.apiSecurity.jwt.exception.InvalidTokenException;
import org.mbc.czo.function.apiSecurity.jwt.repository.UserRefreshTokenRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Log4j2
@Service("JwtServiceImpl")
@RequiredArgsConstructor // final붙은 필드를 생성자로
public class JwtServiceImpl implements JwtService {

    private final MemberJpaRepository memberJpaRepository;  // member db 처리용
    private final JwtManager jwtManager;
    private final PasswordEncoder passwordEncoder;          // 패스워드 암호화
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;           // 토큰 암호화

    @Transactional
    @Override
    public MemberLoginRes login(MemberLoginReq memberLoginReq)throws ApiJwtException {

        Member member = memberJpaRepository.findById(memberLoginReq.getMid())
                .orElseThrow(() -> new ApiJwtException("login: User not found"));

        if (!passwordEncoder.matches(memberLoginReq.getMpassword(), member.getMpassword())) {
            throw new ApiJwtException("login: Invalid password");
        }

        if(!member.isMActivate()) {
            throw new ApiJwtException("login: isActivate False");
        }

        //초기 StartDate 지정으로 앞으로 refreshToken이 발생해도 맨 처음 date + 남은 시간 만큼만 만료시간을 설정가능
        LocalDateTime firstIssuedAt = LocalDateTime.now();

        MemberLoginRes memberLoginRes = jwtManager.generateTokens(member.getMid(),0 , member.getRoleNames(), firstIssuedAt);

        //refreshToken 저장
        String refreshToken = memberLoginRes.getRefreshToken();
        UserRefreshToken tokenEntity =
                UserRefreshToken.createToken(
                        member, refreshToken, firstIssuedAt,jwtManager.getRefreshValidityMs(),
                        refreshTokenHasher,0, jwtManager.getRefreshKey());

        userRefreshTokenRepository.save(tokenEntity);

        return memberLoginRes;
    }


    @Transactional
    @Override
    public ResponseCookie setCookie(String refreshToken) throws ApiJwtException {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)        // JS 접근 불가
                .secure(false)         // HTTPS 환경에서는 true로 설정
                .sameSite("Strict")    // CSRF 방지
                .path("/")             // 전체 경로에서 유효
                .maxAge(jwtManager.getRefreshValidityMs() / 1000) // RefreshToken 만료 기간
                .build();

        return cookie;
    }

    @Transactional
    @Override
    public RefreshTokenRes refreshAccessToken(String refreshToken) throws ApiJwtException {

        // ================= 1. RefreshToken JWT 검증 =================
        //log.info("refreshAccessToken.refreshAccessToken:" +refreshToken);

        //토큰이 유효한지 최소한의 검증
        Claims claims;
        try {
            claims = jwtManager.validateRefreshToken(refreshToken);

            //validateRefreshToken에서 유효하지 않거나 만료된 토큰이면 InvalidTokenException이 발생
        } catch (InvalidTokenException e) {
            throw new ApiJwtException("Invalid or expired refresh token");
        }

        // ================= 2. 토큰 타입 확인 =================

        //log.info("refreshAccessToken.refreshtyp:" +claims.get("typ", String.class));
        //"typ"는 토큰 타입
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new ApiJwtException("Token is not a refresh token");
        }

        String userId = claims.getSubject();

        //userId와 동일한 Id가 DB에 존재하는지 확인
        Member member = memberJpaRepository.findById(userId)
                .orElseThrow(() -> new ApiJwtException("User not found"));

        String tokenId = claims.getId();
        //log.info("refreshAccessToken.claims.getId:" + claims.getId());
        Integer tokenVersion = claims.get("version", Integer.class);


        // ================= 3. 무결성 검사 =================

        //DB에서 RefreshToken 조회
        //@Lock(PESSIMISTIC_WRITE)동시에 같은 토큰을 갱신/회전하려는 다른 트랜잭션을 대기시키므로 중복 사용을 막는다.
        var oldRefreshToken = userRefreshTokenRepository.findByTokenIdForUpdate(tokenId)
                .orElseThrow(() -> new ApiJwtException("Refresh token not found"));

        //refreshToken과 db에 저장된 암호환된 토큰이 같은지 확인
        if (!refreshTokenHasher.matches(refreshToken, oldRefreshToken.getHashedToken())) {
            throw new ApiJwtException("Refresh token mismatch");
        }

        //refreshToken을 요청한 사용자가 맞는지 확인
        if (!oldRefreshToken.getMember().getMid().equals(userId)) {
            throw new ApiJwtException("Token does not belong to user");
        }

        //replacedDate != null이면 이미 rotate가 발생함 || 만료일이 현 시각보다 이전이면 만료된 토큰
        if (oldRefreshToken.getReplacedDate() != null || oldRefreshToken.getExpiresDate().isBefore(LocalDateTime.now())) {
            throw new ApiJwtException("Refresh token expired or revoked");
        }

        //토큰의 버전 체크
        if (!oldRefreshToken.getTokenVersion().equals(tokenVersion)) {
            throw new ApiJwtException("Token version mismatch");
        }

        //RefreshToken 회전(폐기)
        oldRefreshToken.rotate(); // 기존 토큰 무효화
        userRefreshTokenRepository.save(oldRefreshToken);

        //버전 업
        int newVersion = oldRefreshToken.getTokenVersion() + 1;

        //새로운 AccessToken, RefreshToken 발급
        LocalDateTime firstIssuedAt = oldRefreshToken.getFirstIssuedAt();
        Set<String> roles = member.getRoleNames();

        String newAccessToken = jwtManager.generateAccessToken(userId, newVersion, roles);
        String newRefreshToken = jwtManager.generateRefreshToken(userId, newVersion, firstIssuedAt);



        // 신규 RefreshToken도 DB 저장
        //rotate시 이미 버전을 1올림
        UserRefreshToken newTokenEntity =
                UserRefreshToken.createToken(member, newRefreshToken, firstIssuedAt,jwtManager.getRefreshValidityMs(),
                        refreshTokenHasher, newVersion, jwtManager.getRefreshKey());
        userRefreshTokenRepository.save(newTokenEntity);

        return RefreshTokenRes.createRefreshTokenRes(newAccessToken, newRefreshToken);
    }

    @Transactional
    @Override
    public void logout(HttpServletResponse response, Authentication authentication) throws ApiJwtException {

        // authentication은 무조건 null 아님
        String userId = authentication.getName();

        // 서버 측 RefreshToken 삭제
        int deleted = userRefreshTokenRepository.deleteByMember_Mid(userId);
        if (deleted == 0) {
            throw new ApiJwtException("logout:deleted fail");
        }

        // 쿠키 만료
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

    }

}
