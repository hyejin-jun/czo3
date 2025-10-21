package org.mbc.czo.function.review.repository;

import org.mbc.czo.function.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItem_Id(Long itemId);

    List<Review> findByMember_Mid(String memberId);

    // ⭐ 새 메서드: 회원 + 상품 필터
    List<Review> findByMember_MidAndItem_Id(String memberId, Long itemId);
}
