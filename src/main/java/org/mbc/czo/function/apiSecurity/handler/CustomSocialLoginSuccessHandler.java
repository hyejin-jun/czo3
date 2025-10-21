package org.mbc.czo.function.apiSecurity.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiSecurity.jwt.JwtManager;
import org.mbc.czo.function.apiSecurity.jwt.domain.UserRefreshToken;
import org.mbc.czo.function.apiSecurity.jwt.repository.UserRefreshTokenRepository;
import org.mbc.czo.function.apiSecurity.jwt.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;

import org.mbc.czo.function.apiSecurity.jwt.RefreshTokenHasher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Log4j2
@Service // Bean 등록
@RequiredArgsConstructor // final 필드 자동 생성자
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtManager jwtManager;
    private final JwtService jwtService;

    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("CustomSocialLoginSuccessHandler.onAuthenticationSuccess 실행");

        // ---------------- OAuth2User 정보 가져오기 ----------------
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("username");

        // ---------------- DB 회원 조회 ----------------
        Member member = memberJpaRepository.findByMemail(email)
                .orElseThrow(() -> new IllegalStateException("소셜 로그인 회원을 찾을 수 없음: " + email));

        // ---------------- JWT 발급 ----------------
        LocalDateTime firstIssuedAt = LocalDateTime.now();
        Set<String> roles = member.getRoleNames();

        var tokenRes = jwtManager.generateTokens(member.getMid(), 0, roles, firstIssuedAt);

        // ---------------- RefreshToken 저장 ----------------
        UserRefreshToken tokenEntity = UserRefreshToken.createToken(
                member,
                tokenRes.getRefreshToken(),
                firstIssuedAt,
                jwtManager.getRefreshValidityMs(),
                refreshTokenHasher,
                0,
                jwtManager.getRefreshKey()
        );
        userRefreshTokenRepository.save(tokenEntity);

        // ---------------- RefreshToken 쿠키 생성 ----------------
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenRes.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtManager.getRefreshValidityMs() / 1000)
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // ---------------- AccessToken 응답 ----------------
        request.setAttribute("accessToken", tokenRes.getAccessToken());
        request.getRequestDispatcher("/members/oauth2/success").forward(request, response);
    }
}

