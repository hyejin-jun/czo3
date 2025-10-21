package org.mbc.czo.function.review.controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.mbc.czo.function.product.domain.Item;
import org.mbc.czo.function.product.repository.ItemRepository;
import org.mbc.czo.function.review.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews/view")
public class ReviewViewController {

    private final ReviewService reviewService;
    private final ItemRepository itemRepository;
    private final MemberJpaRepository memberRepository;

    // 리뷰 작성 페이지 열기 (주문한 상품 자동)
    @GetMapping("/open")
    public String openReviewPage(@RequestParam Long itemId, Model model) {

        String currentMemberId = getTestMemberId();

        System.out.println("사용할 회원 ID: " + currentMemberId);

        // itemId로 상품 조회
        Item orderedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        model.addAttribute("memberId", currentMemberId);
        model.addAttribute("orderedItem", orderedItem);

        return "review/review";
    }

    // 특정 상품 리뷰 목록 페이지 열기
    @GetMapping("/list/{itemId}")
    public String reviewList(@PathVariable Long itemId, Model model) {
        model.addAttribute("reviews", reviewService.getReviewListByItem(itemId));
        model.addAttribute("averageRating", reviewService.getAverageRating(itemId));
        return "apiMember/apiUserMyPage";  // ReviewList.html 파일명에 맞게 수정
    }

    /**
     * 테스트용 사용자 ID 가져오기
     */
    private String getTestMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("=== 인증 정보 확인 ===");
        System.out.println("Authentication: " + auth);
        System.out.println("Name: " + (auth != null ? auth.getName() : "null"));
        System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));
        System.out.println("Principal 타입: " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null"));
        System.out.println("isAuthenticated: " + (auth != null ? auth.isAuthenticated() : "null"));

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String memberId = auth.getName();
            if (memberRepository.existsById(memberId)) {
                System.out.println("✅ Spring Security에서 유효한 사용자 ID 확인: " + memberId);
                return memberId;
            } else {
                System.out.println("❌ Spring Security ID는 있지만 DB에 없음: " + memberId);
            }
        }

        // DB에서 첫 번째 사용자 가져오기 (테스트용)
        return memberRepository.findAll().stream()
                .findFirst()
                .map(member -> {
                    System.out.println("🧪 테스트용 사용자 사용: " + member.getMid());
                    return member.getMid();
                })
                .orElseThrow(() -> new RuntimeException("테스트용 회원이 DB에 없습니다"));
    }
}
