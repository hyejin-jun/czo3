package org.mbc.czo.function.review.service;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.mbc.czo.function.review.domain.Review;
import org.mbc.czo.function.review.domain.ReviewImg;
import org.mbc.czo.function.review.dto.ReviewRequestDTO;
import org.mbc.czo.function.review.dto.ReviewResponseDTO;
import org.mbc.czo.function.review.repository.ReviewImgRepository;
import org.mbc.czo.function.review.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final MemberJpaRepository memberRepository;
    private final ItemRepository itemRepository;

    @Value("${app.upload.review.path:uploads/review/}")
    private String uploadPath;

    private String absoluteUploadPath;

    /**
     * 서비스 초기화 시 업로드 디렉토리 생성
     */
    @PostConstruct
    public void initUploadDirectory() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!uploadDir.isAbsolute()) {
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            }

            absoluteUploadPath = uploadDir.toString();

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("✅ 업로드 디렉토리 생성됨: " + absoluteUploadPath);
            } else {
                System.out.println("✅ 업로드 디렉토리 확인됨: " + absoluteUploadPath);
            }

            if (!Files.isWritable(uploadDir)) {
                System.err.println("❌ 업로드 디렉토리에 쓰기 권한이 없습니다: " + absoluteUploadPath);
            }

        } catch (IOException e) {
            System.err.println("❌ 업로드 디렉토리 생성 실패: " + e.getMessage());
            throw new RuntimeException("업로드 디렉토리 초기화 실패", e);
        }
    }

    // ================== 리뷰 등록 (핵심 로직 유지) ==================
    public ReviewResponseDTO createReview(ReviewRequestDTO dto) {

        // ⭐ 1. 테스트용 getCurrentMemberId()를 호출하여 ID를 가져옵니다.
        String currentMemberId = getCurrentMemberId();
        System.out.println("리뷰 작성 사용자: " + currentMemberId);

        // ⭐ 2. Member 객체를 가져온 ID로 조회합니다.
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다: " + currentMemberId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + dto.getItemId()));

        Review review = new Review();
        review.setMember(member);
        review.setItem(item);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setVerifiedPurchase(dto.isVerifiedPurchase());

        // 이미지 처리 (기존 로직 유지)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            System.out.println("이미지 파일 개수: " + dto.getImages().size());

            for (MultipartFile file : dto.getImages()) {
                if (!file.isEmpty()) {
                    try {
                        ReviewImg img = saveImageFile(file);
                        review.addImage(img);
                        System.out.println("✅ 이미지 저장 완료: " + img.getStoreFileName());
                    } catch (IOException e) {
                        System.err.println("❌ 이미지 저장 실패: " + file.getOriginalFilename());
                        throw new RuntimeException("이미지 저장 실패: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        Review saved = reviewRepository.save(review);
        System.out.println("✅ 리뷰 저장 완료 ID: " + saved.getId());

        return new ReviewResponseDTO(saved);
    }

    /**
     * 이미지 파일 저장 처리 (기존 로직 유지)
     */
    private ReviewImg saveImageFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String ext = getFileExtension(originalFilename);
        if (!isValidImageExtension(ext)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + ext);
        }

        String storeFileName = UUID.randomUUID().toString() + ext;
        Path filePath = Paths.get(absoluteUploadPath, storeFileName);

        Path parentDir = filePath.getParent();
        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        Files.copy(file.getInputStream(), filePath);

        ReviewImg img = new ReviewImg();
        img.setUploadFileName(originalFilename);
        img.setStoreFileName(storeFileName);
        img.setImageUrl("/uploads/review/" + storeFileName);

        return img;
    }

    /**
     * 파일 확장자 추출 (기존 로직 유지)
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * 허용된 이미지 확장자인지 확인 (기존 로직 유지)
     */
    private boolean isValidImageExtension(String ext) {
        return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") ||
                ext.equals(".gif") || ext.equals(".webp") || ext.equals(".bmp");
    }

    // ================== 현재 사용자 ID 가져오기 (테스트 환경 복원) ==================
    /**
     * 현재 사용자 ID 가져오기: 인증 정보를 무시하고 DB의 첫 번째 사용자 ID를 테스트용으로 반환
     */
    private String getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Spring Security에 유효한 ID가 있으면 반환 (보안성 약간 유지)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String memberId = auth.getName();
            if (memberRepository.existsById(memberId)) {
                System.out.println("✅ Security Context ID 사용: " + memberId);
                return memberId;
            }
        }

        // 2. 유효한 인증 정보가 없거나 DB에 없으면, DB의 첫 번째 사용자 ID를 테스트용으로 반환
        System.out.println("❌ 인증 정보 불확실. DB 첫 번째 사용자 ID를 테스트용으로 사용.");
        return memberRepository.findAll().stream()
                .findFirst()
                .map(member -> {
                    System.out.println("🧪 테스트용 사용자 ID 사용: " + member.getMid());
                    return member.getMid();
                })
                .orElseThrow(() -> new RuntimeException("테스트용 회원이 DB에 없습니다."));
    }

    // 기존 메서드들 (변경 없음)
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewListByItem(Long itemId) {
        return reviewRepository.findByItem_Id(itemId)
                .stream()
                .map(ReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByMember(String memberId) {
        return reviewRepository.findByMember_Mid(memberId)
                .stream()
                .map(ReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long itemId) {
        var reviews = reviewRepository.findByItem_Id(itemId);
        if (reviews.isEmpty()) return 0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemId));
    }

    @Transactional(readOnly = true)
    public Item getLatestOrderedItemForCurrentUser() {
        return itemRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문한 상품이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByMemberAndItem(String memberId, Long itemId) {
        return reviewRepository.findByMember_MidAndItem_Id(memberId, itemId)
                .stream()
                .map(ReviewResponseDTO::new)
                .collect(Collectors.toList());
    }


}
