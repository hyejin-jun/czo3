package org.mbc.czo.function.apiSecurity.jwt;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

    private final PasswordEncoder encoder;

    public RefreshTokenHasher() {
        this.encoder = new BCryptPasswordEncoder();
    }


    //리프레시 토큰 원문을 암호화, DB에 저장할 값으로 변환

    //DigestUtils는 토큰을 SHA-256 같은 해시로 바꾸기 쉽게 도와주는 유틸
    //token 그대로 BCryptPasswordEncoder암호화 하면 72바이트 넘어서 오류 고로 SHA-256로 변환해서 고정 길이
    //원래 토큰 → SHA-256 → BCrypt → DB 저장용 해시
    public String hash(String token) {
        String sha256Hex = DigestUtils.sha256Hex(token); // 고정 64자 길이
        return encoder.encode(sha256Hex);
    }


    //요청받은 리프레시 토큰이 DB의 해시값과 일치하는지 확인
    //SHA-256으로 먼저 변환해야 DB에 저장된 값과 같은 기준으로 비교할 수 있음
    public boolean matches(String token, String hashedToken) {

        String sha256Hex = DigestUtils.sha256Hex(token);
        return encoder.matches(sha256Hex, hashedToken);
    }
}