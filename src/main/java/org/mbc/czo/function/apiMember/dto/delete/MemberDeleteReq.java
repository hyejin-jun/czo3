package org.mbc.czo.function.apiMember.dto.delete;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MemberDeleteReq {

    private String mid;

    private boolean mActivate;
}
