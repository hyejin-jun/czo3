package org.mbc.czo.function.apiMember.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members")
public class ApiMemberPageController {

    // ================= 회원 가입 =================
    @GetMapping("/join")
    public String joinPage() {
        return "apiMember/apiJoin"; // templates/apiMember/apiJoin.html
    }

    // ================= 로그인 =================
    @GetMapping("/login")
    public String loginPage() {
        return "apiMember/apiLogin"; // templates/apiMember/apiLogin.html
    }

    // ================= kakao login success =================
    @GetMapping("/oauth2/success")
    public String kakaoLoginSuccessPage() {
        return "redirect:/member/socialLoginPopupClose.html"; //static/member/socialLoginPopupClose.html
    }

    // ================= 아이디 찾기 =================
    @GetMapping("/findId")
    public String findIdPage() {
        return "apiMember/apiFindId"; // templates/apiMember/apiFindId.html
    }

    // ================= 비밀번호 찾기 =================
    @GetMapping("/findPw")
    public String findPwPage() {
        return "apiMember/apiFindPw"; // templates/apiMember/apiFindPw.html
    }

    // ================= 비밀번호 초기화 =================
    @GetMapping("/resetPw")
    public String resetPwPage() {
        return "apiMember/apiResetPw"; // templates/apiMember/apiResetPw.html
    }

    // ================= 유저 마이 페이지 =================
    @GetMapping("/userMyPage")
    public String userMyPage() {
        return "apiMember/apiUserMyPage"; // templates/apiMember/apiUserMyPage.html
    }

    // ================= 정보수정 =================
    @GetMapping("/modify")
    public String modifyPage() {
        return "apiMember/apiModify"; // templates/apiMember/apiModify.html
    }

}
