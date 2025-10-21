package org.mbc.czo.function.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.cart.constant.DeliveryStatus;
import org.mbc.czo.function.cart.domain.Delivery;
import org.mbc.czo.function.cart.domain.Order;
import org.mbc.czo.function.cart.repository.DeliveryRepository;
import org.mbc.czo.function.cart.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    // 배송 정보 저장
    @Transactional
    public void saveDelivery(Long orderId, String receiverName,
                             String receiverPhone, String address,
                             String detailAddress) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setReceiverName(receiverName);
        delivery.setReceiverPhone(receiverPhone);
        delivery.setAddress(address);
        delivery.setDetailAddress(detailAddress);
        delivery.setStatus(DeliveryStatus.READY); // 초기 상태

        // ★ Order 객체에 delivery 세팅 ★
        order.setDelivery(delivery);

        deliveryRepository.save(delivery);
    }


    // 배송 상태 업데이트
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        delivery.setStatus(status);
    }

    /**
     * 특정 회원의 이전 배송지 목록 조회 (최근 10개)
     */
    public List<DeliveryResponse> getDeliveryListByMemberId(String memberId) {
        try {
            // 해당 회원의 주문들을 찾기
            List<Order> orders = orderRepository.findByMemberMidOrderByOrderDateDesc(memberId);

            // 중복 제거를 위한 Set 사용 (배송지 정보가 같으면 중복으로 간주)
            List<DeliveryResponse> uniqueDeliveries = new ArrayList<>();
            Set<String> addressSet = new HashSet<>();

            for (Order order : orders) {
                Delivery delivery = order.getDelivery();
                if (delivery != null) {
                    // 주소 조합으로 중복 체크
                    String addressKey = delivery.getReceiverName() + "|" +
                            delivery.getAddress() + "|" +
                            delivery.getDetailAddress();

                    if (!addressSet.contains(addressKey)) {
                        addressSet.add(addressKey);
                        uniqueDeliveries.add(new DeliveryResponse(
                                delivery.getId(),
                                delivery.getReceiverName(),
                                delivery.getReceiverPhone(),
                                delivery.getAddress(),
                                delivery.getDetailAddress()
                        ));

                        // 최대 10개까지만
                        if (uniqueDeliveries.size() >= 10) {
                            break;
                        }
                    }
                }
            }

            return uniqueDeliveries;

        } catch (Exception e) {
            log.error("배송지 목록 조회 중 오류: ", e);
            return new ArrayList<>(); // 빈 리스트 반환
        }
    }

    /**
     * 배송지 ID로 배송지 정보 조회
     */
    public Delivery findById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송지 정보를 찾을 수 없습니다. ID: " + deliveryId));
    }

    /**
     * 해당 배송지가 특정 회원의 것인지 확인
     */
    public boolean isDeliveryOwnedByMember(Long deliveryId, String memberId) {
        try {
            Delivery delivery = findById(deliveryId);
            Order order = delivery.getOrder();
            // Member 엔티티의 mid 필드로 비교
            return order != null && order.getMember() != null &&
                    memberId.equals(order.getMember().getMid());
        } catch (Exception e) {
            log.error("배송지 소유권 확인 중 오류: ", e);
            return false;
        }
    }

    // DeliveryResponse DTO 클래스
    public static class DeliveryResponse {
        private Long id;
        private String receiverName;
        private String receiverPhone;
        private String address;
        private String detailAddress;

        public DeliveryResponse(Long id, String receiverName, String receiverPhone, String address, String detailAddress) {
            this.id = id;
            this.receiverName = receiverName;
            this.receiverPhone = receiverPhone;
            this.address = address;
            this.detailAddress = detailAddress;
        }

        // Getters
        public Long getId() { return id; }
        public String getReceiverName() { return receiverName; }
        public String getReceiverPhone() { return receiverPhone; }
        public String getAddress() { return address; }
        public String getDetailAddress() { return detailAddress; }
    }
}