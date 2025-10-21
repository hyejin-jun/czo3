package org.mbc.czo.function.apiUploadImage.service;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.apiUploadImage.domain.MemberProfileImage;
import org.mbc.czo.function.apiUploadImage.dto.delete.ImageDeleteRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRef;
import org.mbc.czo.function.apiUploadImage.dto.upload.ImageUploadRes;
import org.mbc.czo.function.apiUploadImage.repository.MemberProfileImageJpaRepository;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiMemberProfileImageUploadService implements ApiImageUploader {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberProfileImageJpaRepository profileImageJpaRepository;

    @Override
    @Transactional
    public ImageUploadRes upload(ImageUploadRef imageUploadRef) throws IOException {

        List<String> savedUrls = new ArrayList<>();
        for (MultipartFile file : imageUploadRef.getFiles()) {
            if (file.isEmpty()) continue;

            String userId =  imageUploadRef.getExtraData().get("userId");
            Member member = memberJpaRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

            String relativePath = ImageUploadUtils.saveFile(file, "profile");
            String originalName = file.getOriginalFilename();
            String storedName = Paths.get(relativePath).getFileName().toString();

            Optional<MemberProfileImage> existingOpt = profileImageJpaRepository.findByMember_Mid(userId);
            MemberProfileImage profileImage;

            if (existingOpt.isPresent()) {
                profileImage = existingOpt.get();
                // 기존 파일 삭제
                ImageUploadUtils.deleteFile(profileImage.getUploadPath());

                profileImage.setOriginalFileName(originalName);
                profileImage.setStoredFileName(storedName);
                profileImage.setUploadPath(relativePath);
            } else {
                profileImage = new MemberProfileImage(originalName, storedName, relativePath, member);
            }

            profileImageJpaRepository.save(profileImage);
            member.setProfileImage(profileImage);
            memberJpaRepository.save(member);

            savedUrls.add("/uploads/" + relativePath);
        }
        return ImageUploadRes.createImageUploadRes(savedUrls, null);
    }

    @Override
    public void deleteImage(ImageDeleteRef imageDeleteRef) throws IOException {
        //memberProdile은 중복 이미지가 없기에
        //프로필이 수정되면 이전 프로필을 자동 삭제함
        //그러므로 해당 로직은 필요없음
    }
}
