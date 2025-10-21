package org.mbc.czo.function.apiSecurity.jwt.service;

import jakarta.servlet.http.HttpServletResponse;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginReq;
import org.mbc.czo.function.apiSecurity.jwt.dto.login.MemberLoginRes;
import org.mbc.czo.function.apiSecurity.jwt.dto.refreshToken.RefreshTokenRes;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

public interface JwtService {

    //ApiJwtException error
    static class ApiJwtException extends Exception {

        public ApiJwtException(String message) {
            super(message); // 부모 Exception 클래스에 메시지 전달
        }

    }

    MemberLoginRes login(MemberLoginReq memberLoginReq)throws ApiJwtException;

    RefreshTokenRes refreshAccessToken(String refreshToken)throws ApiJwtException;

    ResponseCookie setCookie(String refreshToken)throws ApiJwtException;

    void logout(HttpServletResponse response, Authentication authentication)throws ApiJwtException;
}
