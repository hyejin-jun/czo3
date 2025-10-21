package org.mbc.czo.function.apiSecurity.jwt.dto.refreshToken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRes {

    private String accessToken;
    private String refreshToken;

    public static RefreshTokenRes createRefreshTokenRes(String accessTokenParam, String refreshTokenParam) {
        return  RefreshTokenRes.builder()
                .accessToken(accessTokenParam)
                .refreshToken(refreshTokenParam)
                .build();
    }
}
