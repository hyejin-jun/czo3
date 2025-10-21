package org.mbc.czo.function.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.czo.function.apiMember.dto.info.MemberInfoRes;
import org.mbc.czo.function.apiMember.service.ApiMemberAuthService;
import org.mbc.czo.function.cart.domain.Delivery;
import org.mbc.czo.function.cart.domain.Order;
import org.mbc.czo.function.cart.dto.CartOrderDTO;
import org.mbc.czo.function.cart.dto.OrderDTO;
import org.mbc.czo.function.cart.dto.OrderHistDTO;
import org.mbc.czo.function.cart.repository.OrderRepository;
import org.mbc.czo.function.cart.service.CartService;
import org.mbc.czo.function.cart.service.DeliveryService;
import org.mbc.czo.function.cart.service.OrderService;
import org.mbc.czo.function.apiMember.domain.Member;
import org.mbc.czo.function.apiMember.repository.MemberJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.0.183:3000")
@RequestMapping("/cart/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final MemberJpaRepository memberJpaRepository;
    private final OrderRepository orderRepository;
    private final DeliveryService deliveryService;
    private final ApiMemberAuthService apiMemberAuthService;

    /* ============================
       단일 상품 바로 주문 (상세페이지)
       POST /cart/order/single
       ============================ */
    @GetMapping("/api/order")
    @ResponseBody
    public ResponseEntity<?> getOrderPageData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberId = authentication.getName();
        List<CartOrderDTO> cartItems = cartService.getCartOrdersForUser(memberId);
        int totalPrice = cartItems.stream()
                .mapToInt(CartOrderDTO::getTotalPrice)
                .sum();

        Map<String, Object> response = Map.of(
                "cartItems", cartItems,
                "totalPrice", totalPrice
        );
        log.info("getOrderPageData 호출됨");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/single")
    public @ResponseBody ResponseEntity<?> orderSingleItem(@RequestBody @Valid OrderDTO orderDTO,
                                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("redirect:/members/login");
        }

        String memberId = authentication.getName();
        Long orderId = orderService.order(orderDTO, memberId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @PostMapping("/multi")
    public @ResponseBody ResponseEntity<?> orderMultiItems(@RequestBody @Valid List<OrderDTO> orderDTOList,
                                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("redirect:/members/login");
        }

        String memberId = authentication.getName();
        Long orderId = orderService.orders(orderDTOList, memberId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @PostMapping("/cart")
    public @ResponseBody ResponseEntity<?> orderCartItems(@RequestBody CartOrderDTO cartOrderDTO,
                                                          Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("redirect:/members/login");
        }

        String memberId = authentication.getName();
        List<CartOrderDTO> orderList = cartOrderDTO.getCartOrderDTOList();

        if (orderList == null || orderList.isEmpty()) {
            return new ResponseEntity<>("주문할 상품을 선택해주세요", HttpStatus.BAD_REQUEST);
        }

        for (CartOrderDTO order : orderList) {
            if (!cartService.isCartItemOwnedByUser(order.getCartItemId(), memberId)) {
                return new ResponseEntity<>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
            if (order.getCount() <= 0 || order.getPrice() < 0) {
                return new ResponseEntity<>("잘못된 상품 수량/가격입니다.", HttpStatus.BAD_REQUEST);
            }
        }

        Long orderId = cartService.orderCartItem(orderList, memberId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @PostMapping("/all")
    public String orderFromCart(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/members/login";
        }

        String memberId = authentication.getName();
        orderService.orderFromCart(memberId);

        return "redirect:/cart/order/history";
    }

    @GetMapping("/history")
    public String showOrderHistoryPage() {
        return "order/orderHist";
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<?> getOrderHistory(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<OrderHistDTO> allOrders = new ArrayList<>(cartService.getOrderHist(authentication.getName()));
        allOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));

        int totalOrders = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalOrders);

        if (fromIndex >= totalOrders || fromIndex < 0) {
            return ResponseEntity.badRequest().body("잘못된 페이지 번호입니다.");
        }

        List<OrderHistDTO> orders = allOrders.subList(fromIndex, toIndex);

        Map<Long, Integer> orderTotalMap = new HashMap<>();
        for (OrderHistDTO order : orders) {
            int total = order.getOrderItems().stream()
                    .mapToInt(item -> item.getOrderPrice() * item.getCount())
                    .sum();

            total += order.getShippingFee();

            orderTotalMap.put(order.getOrderId(), total);
            order.setMemberId(authentication.getName());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("orderTotalMap", orderTotalMap);
        response.put("page", page);
        response.put("totalPages", totalPages);

        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public String showOrderPage(Model model, Authentication authentication) {
        return "cart/order";
    }

    @PostMapping("/complete")
    public String completeOrder(@RequestParam("paymentMethod") String paymentMethod,
                                @RequestParam("recipientName") String recipientName,
                                @RequestParam("address") String address,
                                @RequestParam("detailAddress") String detailAddress,
                                @RequestParam("phone") String phone,
                                @RequestParam(value = "isExistingAddress", required = false) String isExistingAddress,
                                Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/members/login";
        }

        String memberId = authentication.getName();

        try {
            List<CartOrderDTO> cartOrders = cartService.getCartOrdersForUser(memberId);
            Long orderId = cartService.orderCartItem(cartOrders, memberId);

            // 기존 주소가 아닌 경우에만 배송 정보 DB 저장
            if (isExistingAddress == null || !isExistingAddress.equals("true")) {
                // 새로 입력된 주소인 경우에만 저장
                deliveryService.saveDelivery(orderId, recipientName, phone, address, detailAddress);
                log.info("새로운 배송지 저장됨 - orderId: {}, recipientName: {}", orderId, recipientName);
            } else {
                log.info("기존 배송지 사용 - 저장 스킵됨 - orderId: {}", orderId);
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalStateException("주문 정보를 찾을 수 없습니다."));

            int totalPrice = order.getTotalPrice() + order.getShippingFee();
            model.addAttribute("orderId", orderId);
            model.addAttribute("totalPrice", totalPrice);

        } catch (Exception e) {
            log.error("주문 처리 중 오류 발생", e);
            model.addAttribute("orderId", 999L);
            model.addAttribute("totalPrice", 0);
        }

        model.addAttribute("paymentMethod", paymentMethod);
        return "order/orderComplete";
    }

    @PostMapping("/cancel")
    @ResponseBody
    public String cancelOrder(@RequestParam Long orderId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return "unauthorized";
        }

        String memberId = authentication.getName();
        orderService.cancelOrder(orderId, memberId);

        return "success";
    }

    @PostMapping("")
    @ResponseBody
    public ResponseEntity<?> orderCart(@RequestBody CartOrderDTO cartOrderDTO,
                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("redirect:/members/login");
        }

        String memberId = authentication.getName();
        Long orderId = cartService.orderCartItem(cartOrderDTO.getCartOrderDTOList(), memberId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    /* ============================
       로그인한 회원정보 반환 (MemberResponse 그대로)
       GET /cart/order/member-info
       ============================ */
    @GetMapping("/member-info")
    @ResponseBody
    public ResponseEntity<?> getMemberInfo(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberId = principal.getName();

        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        String baseAddress = member.getMaddress() != null ? member.getMaddress() : "";
        String detailAddress = member.getMdetailAddress() != null ? member.getMdetailAddress() : "";

        return ResponseEntity.ok(new MemberResponse(
                member.getMname(),
                baseAddress,
                detailAddress,
                member.getMphoneNumber()
        ));
    }

    /* ============================
       이전 배송지 목록 조회
       GET /cart/order/delivery-list
       ============================ */
    @GetMapping("/delivery-list")
    @ResponseBody
    public ResponseEntity<?> getDeliveryList(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String memberId = principal.getName();

        try {
            List<DeliveryService.DeliveryResponse> deliveryList = deliveryService.getDeliveryListByMemberId(memberId);
            return ResponseEntity.ok(deliveryList);

        } catch (Exception e) {
            log.error("배송지 목록 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 목록 조회에 실패했습니다.");
        }
    }

    /* ============================
       특정 배송지 정보 조회
       GET /cart/order/delivery/{deliveryId}
       ============================ */
    @GetMapping("/delivery/{deliveryId}")
    @ResponseBody
    public ResponseEntity<?> getDeliveryById(@PathVariable Long deliveryId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            Delivery delivery = deliveryService.findById(deliveryId);

            String memberId = principal.getName();
            if (!deliveryService.isDeliveryOwnedByMember(deliveryId, memberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
            }

            DeliveryService.DeliveryResponse response = new DeliveryService.DeliveryResponse(
                    delivery.getId(),
                    delivery.getReceiverName(),
                    delivery.getReceiverPhone(),
                    delivery.getAddress(),
                    delivery.getDetailAddress()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("배송지 정보 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배송지 정보 조회에 실패했습니다.");
        }
    }

    public static class MemberResponse {
        private String name;
        private String address;
        private String detailAddress;
        private String phoneNumber;

        public MemberResponse(String name, String address, String detailAddress, String phoneNumber) {
            this.name = name;
            this.address = address;
            this.detailAddress = detailAddress;
            this.phoneNumber = phoneNumber;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getDetailAddress() { return detailAddress; }
        public String getPhoneNumber() { return phoneNumber; }
    }

   /* 이때부터 혜지니 추가 */

    @GetMapping("/api/order/total")
    @ResponseBody
    public ResponseEntity<?> getTotalSales(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 주문 내역 가져오기
        List<OrderHistDTO> allOrders = cartService.getOrderHist(authentication.getName());

        // 전체 매출 계산
        long totalSales = allOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .mapToLong(item -> item.getOrderPrice() * item.getCount())
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("totalSales", totalSales);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/order/sales")
    @ResponseBody
    public ResponseEntity<?> getSalesData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 1. 전체 주문 가져오기
        List<Order> allOrders = orderService.getAllOrders(); // service에서 OrderRepository.findAll() 호출

        // 2. 날짜별 매출액 계산 (예: 하루 단위)
        Map<String, Long> salesMap = new LinkedHashMap<>();
        for (Order order : allOrders) {
            String date = order.getOrderDate().toLocalDate().toString();
            long total = order.getTotalPrice();
            salesMap.put(date, salesMap.getOrDefault(date, 0L) + total);
        }

        // 3. 전체 총액
        long totalSales = allOrders.stream().mapToLong(Order::getTotalPrice).sum();

        // 4. JSON으로 반환
        Map<String, Object> response = new HashMap<>();
        response.put("salesByDate", salesMap); // 날짜별 매출액
        response.put("totalSales", totalSales); // 전체 총 매출액

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/sales/{type}") /* 날짜 */
    @ResponseBody
    public ResponseEntity<?> getSales(
            @PathVariable("type") String type, // "monthly" or "daily"
            @RequestParam("year") int year,
            @RequestParam(value = "month", required = false) Integer month) {

        List<Order> allOrders = orderService.getAllOrders();

        Map<String, Long> revenueMap = new TreeMap<>();
        Map<String, Long> costMap = new TreeMap<>();
        Map<String, Long> profitMap = new TreeMap<>();

        for (Order order : allOrders) {
            LocalDateTime date = order.getOrderDate();
            if (date.getYear() != year) continue;

            String periodKey;
            if ("monthly".equals(type)) {
                periodKey = String.format("%d-%02d", date.getYear(), date.getMonthValue());
            } else {
                if (month != null && date.getMonthValue() != month) continue;
                periodKey = String.format("%d-%02d-%02d",
                        date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            }

            long revenue = order.getTotalPrice();

            // 👈 여기서 cost 계산
            long cost = order.getOrderItems().stream()
                    .mapToLong(oi -> oi.getCostPrice() * oi.getCount()) // cost_price 사용
                    .sum();

            long profit = revenue - cost;

            revenueMap.put(periodKey, revenueMap.getOrDefault(periodKey, 0L) + revenue);
            costMap.put(periodKey, costMap.getOrDefault(periodKey, 0L) + cost);
            profitMap.put(periodKey, profitMap.getOrDefault(periodKey, 0L) + profit);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String period : revenueMap.keySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("period", period);
            map.put("revenue", revenueMap.get(period));
            map.put("cost", costMap.getOrDefault(period, 0L));
            map.put("profit", profitMap.getOrDefault(period, 0L));
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

        // 이번 달 주문 건수 조회
    @GetMapping("/count-this-month")
    public long getOrderCountThisMonth() {
        return orderRepository.countOrdersThisMonth();
    }
}






