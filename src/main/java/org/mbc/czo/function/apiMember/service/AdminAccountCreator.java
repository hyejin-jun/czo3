package org.mbc.czo.function.apiMember.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.constant.Role;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminAccountCreator {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void createDefaultSuperAdmin() {
        String adminId = "superadmin";
        String adminPassword = "admin123!";

        if (memberJpaRepository.existsById(adminId)) {
            log.info("SUPER_ADMIN 계정이 이미 존재합니다: {}", adminId);
            return;
        }

        Member superAdmin = Member.builder()
                .mid(adminId)
                .mname("최고관리자")
                .memail("superadmin@company.com")
                .mphoneNumber("01012345678")
                .mpassword(passwordEncoder.encode(adminPassword))
                .mpostcode("")
                .maddress("")
                .mdetailAddress("")
                .mmileage(0L)
                .mActivate(true)
                .mSocialActivate(false)
                .build();

        superAdmin.addRole(Role.SUPER_ADMIN);
        memberJpaRepository.save(superAdmin);

        log.info("========================================");
        log.info("SUPER_ADMIN 계정 생성 완료!");
        log.info("ID: superadmin");
        log.info("초기 비밀번호: admin123!");
        log.info("========================================");
    }
}