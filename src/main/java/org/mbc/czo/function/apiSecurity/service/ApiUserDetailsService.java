package org.mbc.czo.function.apiSecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.apiSecurity.jwt.JwtManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApiUserDetailsService implements UserDetailsService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtManager jwtManager; // JWT 발급용

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("ApiUserDetailsService.loadUserByUsername 호출됨: " + username);

        // DB에서 사용자 조회
        Member member = memberJpaRepository.getWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 권한 목록 생성
        List<GrantedAuthority> authorities = member.getMroleSet().stream()
                .map(memberRole  -> new SimpleGrantedAuthority("ROLE_" + memberRole.getRole().name()))
                .collect(Collectors.toList());

        // Spring Security User 객체 반환
        return new org.springframework.security.core.userdetails.User(
                member.getMid(),
                member.getMpassword(), // 패스워드 (암호화되어 있어야 함)
                authorities
        );
    }

}
