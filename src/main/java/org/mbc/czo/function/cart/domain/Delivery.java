package org.mbc.czo.function.cart.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.cart.constant.DeliveryStatus;
import org.mbc.czo.function.cart.domain.Order;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Delivery.java
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @ManyToOne
    @JoinColumn(name = "member_mid")
    private Member member;

    private String receiverName;
    private String receiverPhone;
    private String address;
    private String detailAddress;


    // 배송 상태
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.READY;
}
