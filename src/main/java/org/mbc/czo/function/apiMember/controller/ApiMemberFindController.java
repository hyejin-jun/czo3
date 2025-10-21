package org.mbc.czo.function.apiMember.controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.common.apiResult.ApiResult;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdReq;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdRes;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwReq;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwRes;
import org.mbc.czo.function.apiMember.service.ApiMemberFindService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class ApiMemberFindController {

    private final ApiMemberFindService apiMemberFindService;

    //restful한 구조로 가려면 GetMapping을 사용하는게 타당하나, 개인정보 보호를 위해 @RequestBody를 사용
    @PostMapping("/find-id")
    public ApiResult<MemberFindIdRes> findId(@RequestBody MemberFindIdReq memberFindIdReq) {

        try {
            MemberFindIdRes memberFindIdRes = apiMemberFindService.findId(memberFindIdReq);
            return ApiResult.created(memberFindIdRes);

        } catch (ApiMemberFindService.ApiMemberFindException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    //restful한 구조로 가려면 GetMapping을 사용하는게 타당하나, 개인정보 보호를 위해 @RequestBody를 사용
    @PostMapping("/find-pw")
    public ApiResult<MemberFindPwRes> findPw(@RequestBody MemberFindPwReq memberFindPwReq) {

        try {
            MemberFindPwRes memberFindPwRes = apiMemberFindService.findPw(memberFindPwReq);
            return ApiResult.created(memberFindPwRes);

        } catch (ApiMemberFindService.ApiMemberFindException e) {
            return ApiResult.fail(e.getMessage());
        }
    }
}
