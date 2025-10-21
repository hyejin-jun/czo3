package org.mbc.czo.function.apiSecurity.jwt.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginReq;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginRes;
import org.mbc.czo.function.apiSecurity.jwt.dto.refreshToken.RefreshTokenRes;
import org.mbc.czo.function.apiSecurity.jwt.service.JwtService;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

    // ================= Login =================
    @PostMapping("/login")
    public ResponseEntity<ApiResult<MemberLoginRes>> login(
            @RequestBody MemberLoginReq memberLoginReq) {
        try {
            MemberLoginRes memberLoginRes = jwtService.login(memberLoginReq);

            // === RefreshToken을 HttpOnly 쿠키로 내려주기 ===
            ResponseCookie cookie = jwtService.setCookie(memberLoginRes.getRefreshToken());

            // === AccessToken은 ApiResult.data에 포함, RefreshToken은 쿠키로 내려감 ===
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResult.created(memberLoginRes));

        } catch (JwtService.ApiJwtException e) {

            return ResponseEntity
                    .badRequest()
                    .body(ApiResult.fail(e.getMessage()));
        }
    }

    // ================= Refresh Access Token =================

    // Response 객체에 Set-Cookie 추가하려면 Controller에서 처리
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<RefreshTokenRes>> refreshAccessToken(
            @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

        try {
            RefreshTokenRes refreshTokenRes = jwtService.refreshAccessToken(refreshToken);

            // 새 RefreshToken 쿠키 세팅
            ResponseCookie cookie = jwtService.setCookie(refreshTokenRes.getRefreshToken());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(ApiResult.created(refreshTokenRes));

        } catch (JwtService.ApiJwtException e) {

            return ResponseEntity
                    .badRequest()
                    .body(ApiResult.fail(e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()") // 로그인한 상태이면!!! (권한에 상관없음)
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(
            HttpServletResponse response, Authentication authentication) {

        try {
            jwtService.logout(response, authentication);

            return ResponseEntity.ok(ApiResult.none());

        } catch (JwtService.ApiJwtException e) {

            return ResponseEntity
                    .badRequest()
                    .body(ApiResult.fail(e.getMessage()));
        }
    }

}