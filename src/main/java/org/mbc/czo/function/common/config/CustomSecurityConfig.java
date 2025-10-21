package org.mbc.czo.function.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiSecurity.service.ApiUserDetailsService;
import org.mbc.czo.function.apiSecurity.handler.CustomSocialLoginSuccessHandler;
import org.mbc.czo.function.apiSecurity.jwt.JwtAuthenticationFilter;
import org.mbc.czo.function.apiSecurity.service.CustomOAuth2UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;


@Log4j2 // 로그 출력용
@Configuration  // 환경설정임을 명시
@RequiredArgsConstructor // final 필드에 대한 생성자

public class  CustomSecurityConfig {
    // 스프링시큐리티 환경설정 하는 부분
    // board/list 접속시 /login 페이지로 자동 이동(시큐리티 내장된 로그인 페이지 : id (user)
    // Using generated security password: 056b482c-b7f9-4582-8a48-392bdc5e9d55(1회용)

    private final DataSource dataSource;

    private final ApiUserDetailsService apiUserDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomSocialLoginSuccessHandler customSocialLoginSuccessHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {

        log.info("----------------CustomSecurityConfig.filterChain()----------------------");

        /*jwt 필터*/
       http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    http.oauth2Login(oauth2 ->
                oauth2.successHandler(customSocialLoginSuccessHandler)
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
        );


        // http.csrf().disable()
        // 6.1 버전에서 제외 됨 (스프링 3.0이후 버전에서는 사용 안됨)
        // 람다식으로 사용할 것을 권고 함. 아래로 변경
        http.csrf(httpSecurityCsrfConfigurer -> {
            // csrf 토큰에 대한 비활성화
            // 실무에서는 사용하면 안됨
            // 프론트에 아래코드 필수
            // <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
            log.info("======= CSRF 비활성화 호출=======");
            httpSecurityCsrfConfigurer.disable();
        });

        http.authorizeHttpRequests(authorizeHttpRequests -> {
            authorizeHttpRequests
                    .requestMatchers("/ws/chat/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/.well-known/**").permitAll()
                    .requestMatchers("/js/**").permitAll()
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/api/**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/products").permitAll()
                    .requestMatchers("/item/**").permitAll()
                    .requestMatchers("/admin/**").permitAll()
                    .requestMatchers("/board/**").permitAll()
                    .requestMatchers("/member/**").permitAll()
                    .requestMatchers("/members/**").permitAll()
                    .requestMatchers("/cart/**").permitAll()
                    .requestMatchers("/reviews/**").permitAll()
                    .requestMatchers("/oauth2/**").permitAll()

                    // ✅ 관리자만 접근 가능
                    .requestMatchers("/api/salaries/**").hasRole("ADMIN")
                    .requestMatchers("/api/settlement/**").hasRole("ADMIN")
                    .requestMatchers("/api/sales/**").hasRole("ADMIN")

                    .anyRequest().authenticated();
        });

       /* // p718 403예외처리 핸들러 사용
        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
            httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler());
            // 하단에 메서드 추가
        });*/

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // css와 같이 정적 자원들에 대한 시큐리티 적용 제외
        // No security for GET /css/styles.css
        log.info("--- CustomSecurityConfig.WebSecurityCustomizer()---------");

        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean // 자동로그인용 데이터베이스 처리
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        log.info("======= persistentTokenRepository 토큰생성기법 호출 =======");
        return jdbcTokenRepository;
        //https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/authentication/rememberme/PersistentTokenRepository.html
    }

}

