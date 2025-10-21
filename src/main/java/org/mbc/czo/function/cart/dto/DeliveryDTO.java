package org.mbc.czo.function.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbc.czo.function.cart.domain.Delivery;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String detailAddress;

    public static DeliveryDTO fromEntity(Delivery delivery) {
        return DeliveryDTO.builder()
                .id(delivery.getId())
                .receiverName(delivery.getReceiverName())
                .receiverPhone(delivery.getReceiverPhone())
                .address(delivery.getAddress())
                .detailAddress(delivery.getDetailAddress())
                .build();
    }
}
