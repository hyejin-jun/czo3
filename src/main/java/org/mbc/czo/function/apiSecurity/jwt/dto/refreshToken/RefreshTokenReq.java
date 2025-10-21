package org.mbc.czo.function.apiSecurity.jwt.dto.refreshToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenReq {
    private String refreshToken;
}
