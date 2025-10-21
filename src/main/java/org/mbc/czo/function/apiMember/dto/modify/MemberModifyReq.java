package org.mbc.czo.function.apiMember.dto.modify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberModifyReq {

    private String mname;

    private String mphoneNumber;

    private String mnewPassword;

    private String mpostcode;

    private String maddress;

    private String mdetailAddress;

}
