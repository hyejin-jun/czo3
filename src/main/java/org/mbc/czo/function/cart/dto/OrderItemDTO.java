package org.mbc.czo.function.cart.dto;

import lombok.Getter;
import lombok.Setter;
import org.mbc.czo.function.cart.domain.OrderItem;

@Getter
@Setter
public class OrderItemDTO {
    // 상품 식별자 (리뷰 작성에 필요)
    private Long itemId;

    // 주문 상품명
    private String itemNm;

    // 주문수량
    private int count;

    // 주문 당시 상품 가격
    private int orderPrice;
    // 상품이미지 url
    private String imgUrl;

    public OrderItemDTO(OrderItem orderItem, String imgUrl) {
        this.itemId = orderItem.getItem().getId();     // itemId 추가
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.orderPrice = orderItem.getOrderPrice();
        this.imgUrl = imgUrl;
    }
}
