package org.mbc.czo.function.apiMember.dto.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.apiMember.constant.Role;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.domain.MemberRole;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoRes {

    private String mid;

    private String mname;

    private String mphoneNumber;

    private String memail;  // 회원 검색 처리용

    private String mpostcode;

    private String maddress;

    private String mdetailAddress;

    private Set<Role> mroleSet = new HashSet<Role>();

    private Long mmileage;

    private boolean mActivate;

    private boolean mSocialActivate;

    private Map<String, Object> mSocialprops;

    private String profileImagePath;

    public static MemberInfoRes createMemberInfoRes(Member member) {

        MemberInfoRes memberInfoRes = MemberInfoRes.builder()
                .mid(member.getMid())
                .memail(member.getMemail())
                .mActivate(member.isMActivate())
                .mSocialActivate(member.isMSocialActivate())
                .mname(member.getMname())
                .mphoneNumber(member.getMphoneNumber())
                .mpostcode(member.getMpostcode())
                .maddress(member.getMaddress())
                .mdetailAddress(member.getMdetailAddress())
                .mmileage(member.getMmileage())
                .mroleSet(
                        member.getMroleSet().stream()
                                .map(MemberRole::getRole) // MemberRole → Role
                                .collect(Collectors.toSet())
                )
                .build();

        if (member.getProfileImage() != null) {
            String relativePath = member.getProfileImage().getUploadPath();
            memberInfoRes.setProfileImagePath("/uploads/" + relativePath);
        }

        return memberInfoRes;
    }
}
