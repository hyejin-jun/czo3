package org.mbc.czo.function.apiUploadImage.repository;

import org.mbc.czo.function.apiUploadImage.domain.MemberProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProfileImageJpaRepository extends JpaRepository<MemberProfileImage, Long> {

    //MemberProfileImage.member.mid 조회
    Optional<MemberProfileImage> findByMember_Mid(String memberId);
}
