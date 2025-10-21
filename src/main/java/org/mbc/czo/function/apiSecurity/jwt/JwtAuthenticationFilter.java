package org.mbc.czo.function.apiSecurity.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mbc.czo.function.apiSecurity.jwt.exception.InvalidTokenException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtManager jwtManager;

    public JwtAuthenticationFilter(JwtManager jwtManager) {
        this.jwtManager = jwtManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                // AccessToken 검증 및 Claims 추출
                Claims claims = jwtManager.validateAccessToken(token);

                // userId와 roles 추출
                String userId = claims.getSubject();
                List<String> rolesList = claims.get("roles", List.class);

                //roles를 Spring Security 권한 객체(GrantedAuthority)로 변환
                List<GrantedAuthority> authorities = rolesList != null ?
                        rolesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : new ArrayList<>();

                // Authentication 생성
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(userId, "", authorities);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                // SecurityContext에 인증 정보 세팅
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (InvalidTokenException e) {
                // 토큰이 유효하지 않으면 인증 세팅하지 않고 통과
                System.out.println("[JWT Filter] Invalid or expired token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

