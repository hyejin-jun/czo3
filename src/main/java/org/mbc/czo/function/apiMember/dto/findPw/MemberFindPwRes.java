package org.mbc.czo.function.apiMember.dto.findPw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFindPwRes {

    private String mid;

    public static MemberFindPwRes createMemberFindPwRes(String midParam) {
        return MemberFindPwRes.builder()
                .mid(midParam)
                .build();
    }
}
