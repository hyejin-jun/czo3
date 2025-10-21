package org.mbc.czo.function.apiMember.dto.findId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFindIdRes {

    private String mid;

    public static MemberFindIdRes createMemberFindIdRes(String midParam) {
        return MemberFindIdRes.builder()
                .mid(midParam)
                .build();
    }
}
