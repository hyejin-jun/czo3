package org.mbc.czo.function.apiMember.dto.resetPW;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResetPWReq {
    private String mid;

    private String mnewPassword;

}
