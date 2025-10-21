package org.mbc.czo.function.apiMember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.constant.Role;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.apiMember.dto.delete.MemberDeleteReq;
import org.mbc.czo.function.apiMember.dto.info.MemberInfoRes;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinReq;
import org.mbc.czo.function.apiMember.dto.join.MemberJoinRes;
import org.mbc.czo.function.apiMember.dto.modify.MemberModifyReq;
import org.mbc.czo.function.apiMember.dto.myPage.MemberMyPageReq;
import org.mbc.czo.function.apiMember.dto.myPage.MemberMyPageRes;
import org.mbc.czo.function.apiMember.dto.resetPW.MemberResetPWReq;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service("APIMemberAuthServiceImpl")
@RequiredArgsConstructor // final붙은 필드를 생성자로
public class ApiMemberAuthServiceImpl implements ApiMemberAuthService {

    private final MemberJpaRepository memberJpaRepository;  // member db 처리용
    private final PasswordEncoder passwordEncoder;    // 패스워드 암호화

    //기본적으로 @Transactional가 있으면 변경된 값에 대한 자동적인 DB업로드가 되지만
    //안전하게 memberJpaRepository.save(member)까지 추가
    @Transactional
    @Override
    public MemberInfoRes info(Authentication authentication) throws ApiMemberAuthException {

        Member member = memberJpaRepository.findById(authentication.getName())
                .orElseThrow(() -> new ApiMemberAuthException("info: Member not found"));

        return MemberInfoRes.createMemberInfoRes(member);
    }

    @Transactional
    @Override
    public MemberJoinRes join(MemberJoinReq memberJoinReq)throws ApiMemberAuthException {

        // 이미 같은 ID가 있는지 확인
        if (memberJpaRepository.existsById(memberJoinReq.getMid())) {
            throw new ApiMemberAuthException("join: member id already exists");
        }
        // Member 엔티티 생성
        Member member = Member.createApiMember(memberJoinReq, passwordEncoder);
        member.addRole(Role.USER);

        // DB 저장 (kakao 처리 생각해야함)
        memberJpaRepository.save(member);

        // 결과 DTO 반환
        return MemberJoinRes.createMemberJoinRes(member);
    }

    @Transactional
    @Override
    public void resetPW(MemberResetPWReq memberResetPWReq) throws ApiMemberAuthException {

        Member member = memberJpaRepository.findById(memberResetPWReq.getMid())
                .orElseThrow(() -> new ApiMemberAuthException("resetPW: Member not found"));

        member.setMpassword(passwordEncoder.encode(memberResetPWReq.getMnewPassword()));
        memberJpaRepository.save(member); // 트랜잭션 커밋 시 DB 반영
    }

    @Transactional
    @Override
    public void modify(MemberModifyReq memberModifyReq, Authentication authentication) throws ApiMemberAuthException {

        Member member = memberJpaRepository.findById(authentication.getName())
                .orElseThrow(() -> new ApiMemberAuthException("modify: Member not found"));

        member.updateApiMember(memberModifyReq, passwordEncoder);
       /* Member member =  Member.createMember(membermodifyDTO, passwordEncoder);
        member.addRole(Role.USER);*/

        memberJpaRepository.save(member);

    }

    @Transactional
    @Override
    public void delete(MemberDeleteReq memberDeleteReq) throws ApiMemberAuthException {

        Member member = memberJpaRepository.findByMidAndMActivate(memberDeleteReq.getMid(), memberDeleteReq.isMActivate())
                .orElseThrow(() -> new ApiMemberAuthException("delete: Member not found or Already deleted"));

        member.setMActivate(false);
        memberJpaRepository.save(member);
    }

    @Transactional
    @Override
    public MemberMyPageRes myPage(MemberMyPageReq memberMyPageReq) throws ApiMemberAuthException {

        Member member = memberJpaRepository.findById(memberMyPageReq.getMid())
                .orElseThrow(() -> new ApiMemberAuthException("myPage: Member not found"));

        return MemberMyPageRes.createMemberMyPageRes(member);
    }

}
