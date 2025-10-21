package org.mbc.czo.function.apiMember.dto.findId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberFindIdReq {

    private String mname;

    private String mphoneNumber;

}
