package org.mbc.czo.function.cart.repository;

import org.mbc.czo.function.cart.domain.CartItem;
import org.mbc.czo.function.cart.dto.CartDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 1. 장바구니에 특정 상품이 이미 담겨있는지 확인
    Optional<CartItem> findByCart_IdAndItem_Id(Long cartId, Long itemId);

    // 2. 장바구니 상세 정보를 DTO로 조회 (이미지 제외)
    @Query("SELECT new org.mbc.czo.function.cart.dto.CartDetailDTO(" +
            "ci.id, i.itemNm, i.price, ci.count) " +
            "FROM CartItem ci " +
            "JOIN ci.item i " +
            "WHERE ci.cart.id = :cartId " +
            "ORDER BY ci.regTime DESC")
    List<CartDetailDTO> findCartDetailDTOList(Long cartId);

    // 3. 특정 회원 장바구니 상품 개수 조회
    long countByCart_Member_Memail(String email);

}
