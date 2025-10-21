package org.mbc.czo.function.apiMember.dto.join;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinReq {
    private String mid;

    private String mname;

    private String mphoneNumber;

    private String memail;

    private String mpassword;

    private String mpostcode;

    private String maddress;

    private String mdetailAddress;

    private boolean mSocialActivate;
}
