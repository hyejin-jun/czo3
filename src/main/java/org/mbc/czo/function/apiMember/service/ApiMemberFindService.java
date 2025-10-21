package org.mbc.czo.function.apiMember.service;

import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdReq;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdRes;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwReq;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwRes;

public interface ApiMemberFindService {

    //APIMemberAuthService error
    static class ApiMemberFindException extends Exception {

        public ApiMemberFindException(String message) {
            super(message); // 부모 Exception 클래스에 메시지 전달
        }

    }


    public MemberFindIdRes findId(MemberFindIdReq memberFindIdReq)throws ApiMemberFindException;
    public MemberFindPwRes findPw(MemberFindPwReq memberFindPwReq)  throws ApiMemberFindException;
}
