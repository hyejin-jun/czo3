package org.mbc.czo.function.apiMember.service;

import org.mbc.czo.function.apiMember.dto.delete.MemberDeleteReq;
import org.mbc.czo.function.apiMember.dto.info.MemberInfoRes;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinReq;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinRes;
import org.mbc.czo.function.apiMember.dto.modify.MemberModifyReq;
import org.mbc.czo.function.apiMember.dto.myPage.MemberMyPageReq;
import org.mbc.czo.function.apiMember.dto.myPage.MemberMyPageRes;
import org.mbc.czo.function.apiMember.dto.resetPW.MemberResetPWReq;
import org.springframework.security.core.Authentication;

public interface ApiMemberAuthService {

    //APIMemberAuthService error
    static class ApiMemberAuthException extends Exception {

        public ApiMemberAuthException(String message) {
            super(message); // 부모 Exception 클래스에 메시지 전달
        }

    }

    MemberInfoRes info(Authentication authentication)throws ApiMemberAuthException;

    MemberJoinRes join(MemberJoinReq memberJoinReq)throws ApiMemberAuthException;

    void resetPW(MemberResetPWReq memberResetPWReq) throws ApiMemberAuthException;

    void modify(MemberModifyReq memberModifyReq, Authentication authentication) throws ApiMemberAuthException;

    void delete(MemberDeleteReq memberDeleteReq)throws ApiMemberAuthException;

    MemberMyPageRes myPage(MemberMyPageReq memberMyPageReq)throws ApiMemberAuthException;

}
