package org.mbc.czo.function.apiSecurity.jwt.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLoginRes {

    String accessToken;
    String refreshToken;

    public static MemberLoginRes createMemberLoginRes(String accessTokenParam, String refreshTokenParam) {
        return  MemberLoginRes.builder()
                .accessToken(accessTokenParam)
                .refreshToken(refreshTokenParam)
                .build();
    }
}
