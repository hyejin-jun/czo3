package org.mbc.czo.function.review.controller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.review.dto.ReviewResponseDTO;
import org.mbc.czo.function.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 상세 페이지(itemDtl.html)에서 사용하는 API 컨트롤러
 * 정확한 URL: /api/items/{itemId}/reviews
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ItemDetailApiController {

    private final ReviewService reviewService;

    /**
     * 상품 상세 페이지에서 호출하는 리뷰 목록 API
     * GET /api/items/{itemId}/reviews?page=1&size=5
     */
    @GetMapping("/items/{itemId}/reviews")
    public ResponseEntity<Map<String, Object>> getItemReviews(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        System.out.println("==========================================");
        System.out.println("🔍 [새 API 컨트롤러] 리뷰 목록 조회 요청");
        System.out.println("   - URL: /api/items/" + itemId + "/reviews");
        System.out.println("   - 상품ID: " + itemId);
        System.out.println("   - 페이지: " + page + ", 크기: " + size);

        try {
            // 전체 리뷰 목록 조회
            List<ReviewResponseDTO> allReviews = reviewService.getReviewListByItem(itemId);
            System.out.println("📋 DB에서 조회된 전체 리뷰 수: " + allReviews.size());


            // 페이지네이션 처리
            int totalElements = allReviews.size();
            int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

            // 페이지 범위 계산 (1-based를 0-based로 변환)
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<ReviewResponseDTO> pagedReviews;
            if (startIndex < totalElements && startIndex >= 0) {
                pagedReviews = allReviews.subList(startIndex, endIndex);
                System.out.println("📄 페이지 처리 완료 - " + pagedReviews.size() + "개 리뷰 반환");
            } else {
                pagedReviews = List.of();
                System.out.println("📄 해당 페이지에 리뷰 없음 (startIndex: " + startIndex + ")");
            }

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", pagedReviews);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("pageSize", size);

            System.out.println("✅ API 응답 성공:");
            System.out.println("   - 총 리뷰 수: " + totalElements);
            System.out.println("   - 총 페이지 수: " + totalPages);
            System.out.println("   - 현재 페이지 리뷰 수: " + pagedReviews.size());
            System.out.println("==========================================");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌❌❌ 리뷰 목록 조회 실패 ❌❌❌");
            System.err.println("에러 메시지: " + e.getMessage());
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            e.printStackTrace();

            // 에러 발생 시 빈 데이터 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("reviews", List.of());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("pageSize", size);
            errorResponse.put("error", "리뷰 목록을 불러올 수 없습니다: " + e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 상품의 평균 평점 조회 (추가 기능)
     * GET /api/items/{itemId}/rating
     */
    @GetMapping("/items/{itemId}/rating")
    public ResponseEntity<Map<String, Object>> getItemRating(@PathVariable Long itemId) {
        try {
            double averageRating = reviewService.getAverageRating(itemId);
            List<ReviewResponseDTO> reviews = reviewService.getReviewListByItem(itemId);

            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", Math.round(averageRating * 10) / 10.0);
            response.put("totalReviews", reviews.size());

            System.out.println("⭐ 평점 조회 - 상품ID: " + itemId + ", 평균: " + averageRating + ", 총 리뷰: " + reviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 평점 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}