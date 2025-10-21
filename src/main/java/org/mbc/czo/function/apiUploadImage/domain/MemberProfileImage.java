package org.mbc.czo.function.apiUploadImage.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.mbc.czo.function.apiMember.domain.Member;

@Entity
@Table(name="memberProfileImage")
public class MemberProfileImage extends BaseImage {

    //프로필 이미지는 하나
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public MemberProfileImage() {}

    public MemberProfileImage(String originalFileName, String storedFileName, String uploadPath, Member member) {
        super(originalFileName, storedFileName, uploadPath);
        this.member = member;
    }
}
