package org.mbc.czo.function.apiSecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.constant.Role;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException{

        log.info("CustomOAuth2UserService.loadUser 실행");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String clientName = userRequest.getClientRegistration().getClientName();

        // ---------------- 카카오 로그인 처리 ----------------
        final String finalEmail;
        final String finalName;

        if ("kakao".equals(clientName)) {
            Map<String, Object> kakaoAttributes = oAuth2User.getAttributes();
            Map<String, Object> account = (Map<String, Object>) kakaoAttributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) account.get("profile");

            finalEmail = (String) account.get("email");
            finalName = (String) profile.get("nickname");

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 클라이언트: " + clientName);
        }

        log.info("소셜 로그인 사용자 email={}, name={}", finalEmail, finalName);

        // ---------------- DB 조회 및 신규 가입 ----------------
        Member member = memberJpaRepository.findByMemail(finalEmail)
                .orElseGet(() -> createAndSaveSocialMember(finalEmail, finalName));

        // ---------------- Spring Security User 객체 생성 ----------------
        List<GrantedAuthority> authorities = member.getMroleSet().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole().name()))
                .collect(Collectors.toList());

        return new DefaultOAuth2User(
                authorities,                       // Collection<? extends GrantedAuthority>
                Map.of("username", member.getMid()), // attributes (Map<String,Object>)
                "username"
        );
    }

    // ---------------- 신규 소셜 사용자 생성 메서드 ----------------

    private Member createAndSaveSocialMember(String email, String name) {
        log.info("신규 소셜 사용자 자동 가입 처리");
        Member newMember = Member.createNewSocialMember(email,name,passwordEncoder);
        newMember.addRole(Role.USER);

        return memberJpaRepository.save(newMember);
    }
}

