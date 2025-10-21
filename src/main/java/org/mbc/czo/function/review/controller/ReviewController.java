package org.mbc.czo.function.review.controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.review.dto.ReviewRequestDTO;
import org.mbc.czo.function.review.dto.ReviewResponseDTO;
import org.mbc.czo.function.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@ModelAttribute ReviewRequestDTO dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    // 특정 상품 리뷰 목록
    @GetMapping("/item/{itemId}/list")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getReviewListByItem(itemId));
    }

    // 특정 상품 평균 별점 (double 값만 반환)
    @GetMapping("/item/{itemId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getAverageRating(itemId));
    }

    // ✅ 특정 상품 평균 별점 (JSON 객체 반환: { average, rounded })
    @GetMapping("/item/{itemId}/average-rating-detail")
    public ResponseEntity<Map<String, Object>> getAverageRatingDetail(@PathVariable Long itemId) {
        double avg = reviewService.getAverageRating(itemId);
        int rounded = (int) Math.round(avg);

        Map<String, Object> result = new HashMap<>();
        result.put("average", avg);
        result.put("rounded", rounded);

        return ResponseEntity.ok(result);
    }

    // 특정 회원 리뷰 목록
    @GetMapping("/member/{memberId}/list")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByMember(@PathVariable String memberId) {
        return ResponseEntity.ok(reviewService.getReviewsByMember(memberId));
    }
}
