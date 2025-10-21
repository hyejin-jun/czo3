package org.mbc.czo.function.review.dto;

import org.mbc.czo.function.review.domain.Review;

public class ReviewResponseDTO {

    private Long id;
    private String content;
    private int rating;
    private String memberId; // ⭐ 필수 추가

    public ReviewResponseDTO(Review review) {
        this.id = review.getId();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.memberId = review.getMember().getMid(); // 엔티티에서 가져오기
    }

    // Getter
    public Long getId() { return id; }
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public String getMemberId() { return memberId; } // ⭐ 필수
}
