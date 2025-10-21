package org.mbc.czo.function.apiMember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdReq;
import org.mbc.czo.function.apiMember.dto.findId.MemberFindIdRes;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwReq;
import org.mbc.czo.function.apiMember.dto.findPw.MemberFindPwRes;
import org.springframework.stereotype.Service;

@Log4j2
@Service("ApiMemberFindServiceImpl")
@RequiredArgsConstructor // final붙은 필드를 생성자로
public class ApiMemberFindServiceImpl implements ApiMemberFindService {

    private final MemberJpaRepository memberJpaRepository;  // member db 처리용

    public MemberFindIdRes findId(MemberFindIdReq memberFindIdReq) throws ApiMemberFindException {

        String m_name = memberFindIdReq.getMname();
        String m_phoneNumber = memberFindIdReq.getMphoneNumber();

        String resultID =memberJpaRepository.checkExistIDFromNameAndPhoneNumber(m_name, m_phoneNumber)
                .filter(id -> !id.isEmpty()) // 빈 문자열 제거
                .orElseThrow(() -> new ApiMemberFindException("ApiMemberFindServiceImpl.findID not found"));

        log.info("MemberFindIdRes.findid: " + resultID);
        return MemberFindIdRes.createMemberFindIdRes(resultID);
    }

    public MemberFindPwRes findPw(MemberFindPwReq memberFindPwReq) throws ApiMemberFindException {

        String m_email = memberFindPwReq.getMemail();
        String m_name = memberFindPwReq.getMname();

        String resultID =memberJpaRepository.checkExistPWFromIDAndName(m_email, m_name)
                .filter(id -> !id.isEmpty()) // 빈 문자열 제거
                .orElseThrow(() -> new ApiMemberFindException("ApiMemberFindServiceImpl.findPW not found"));

        return MemberFindPwRes.createMemberFindPwRes(resultID);
    }

}
