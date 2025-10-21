package org.mbc.czo.function.apiMember.dto.join;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiMember.domain.Member;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinRes {

    private String mid;

    private String mname;

    private String mphoneNumber;

    private String memail;

    private String mpostcode;

    private String maddress;

    private String mdetailAddress;

    private Long mmileage;

    private boolean mActivate;

    private boolean mSocialActivate;

    public static MemberJoinRes createMemberJoinRes(Member member) {
        return MemberJoinRes.builder()
                .mid(member.getMid())
                .mname(member.getMname())
                .mphoneNumber(member.getMphoneNumber())
                .memail(member.getMemail())
                .mpostcode(member.getMpostcode())
                .maddress(member.getMaddress())
                .mdetailAddress(member.getMdetailAddress())
                .mmileage(member.getMmileage())
                .mActivate(member.isMActivate())
                .mSocialActivate(member.isMSocialActivate())
                .build();
    }
}
