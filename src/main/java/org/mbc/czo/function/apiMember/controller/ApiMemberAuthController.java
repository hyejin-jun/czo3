package org.mbc.czo.function.apiMember.controller;

import lombok.RequiredArgsConstructor;

import org.mbc.czo.function.common.apiResult.ApiResult;
import org.mbc.czo.function.apiMember.dto.delete.MemberDeleteReq;
import org.mbc.czo.function.apiMember.dto.info.MemberInfoRes;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinReq;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinRes;
import org.mbc.czo.function.apiMember.dto.modify.MemberModifyReq;
import org.mbc.czo.function.apiMember.dto.resetPW.MemberResetPWReq;
import org.mbc.czo.function.apiMember.service.ApiMemberAuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class ApiMemberAuthController {

    private final ApiMemberAuthService apiMemberAuthService;


    @GetMapping("/me")
    public ApiResult<MemberInfoRes> info(Authentication authentication) {

        try {
            MemberInfoRes memberInfoRes = apiMemberAuthService.info(authentication);
            return ApiResult.created(memberInfoRes);

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // ================= 회원 가입 =================
    // ("/")생략 가능
    @PostMapping("/")
    public ApiResult<MemberJoinRes> join(@RequestBody MemberJoinReq memberJoinReq) {
        try {
            MemberJoinRes joinedMember = apiMemberAuthService.join(memberJoinReq);
            return ApiResult.created(joinedMember);

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // ================= 비밀번호 초기화 =================
    @PatchMapping("/password")
    public ApiResult<Void> resetPassword(@RequestBody MemberResetPWReq memberResetPWReq) {
        try {
            apiMemberAuthService.resetPW(memberResetPWReq);
            return ApiResult.none();

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // ================= 회원 정보 수정 =================

    // @AuthenticationPrincipal -> user (membersecurityDto) 값 받아오고 싶으면 사용
    //  //세션, JWT토큰에서 모두 사용 (Principal -> 세션 에서만 사용)
    @PreAuthorize("isAuthenticated()") // 로그인한 상태이면!!! (권한에 상관없음)
    @PutMapping("/")
    public ApiResult<MemberJoinRes> modify(
            @RequestBody MemberModifyReq memberModifyReq,
            Authentication authentication) {
        try {
            apiMemberAuthService.modify(memberModifyReq, authentication);
            return ApiResult.none();

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // ================= 회원 삭제 =================

    @PreAuthorize("isAuthenticated()") // 로그인한 상태이면!!! (권한에 상관없음)
    @DeleteMapping("/")
    public ApiResult<Void> deleteMember(@RequestBody MemberDeleteReq memberDeleteReq) {

        try {
            apiMemberAuthService.delete(memberDeleteReq);
            return ApiResult.none();

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }

    // ================= 회원 마이페이지 =================

  /*  @PreAuthorize("isAuthenticated()")  // 로그인한 상태이면!!! (권한에 상관없음)
    @GetMapping("/me")  // 로그인한 사용자의 정보 제공
    public ApiResult<MemberMyPageRes> getMyPage(MemberMyPageReq memberMyPageReq) {

        try {
            MemberMyPageRes memberMyPageRes = apiMemberAuthService.myPage(memberMyPageReq);
            return ApiResult.ok(memberMyPageRes);

        } catch (ApiMemberAuthService.ApiMemberAuthException e) {
            return ApiResult.fail(e.getMessage());
        }
    }*/
}
