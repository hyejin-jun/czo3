package org.mbc.czo.function.salesCha.Cotoroller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.cart.domain.Order;
import org.mbc.czo.function.cart.repository.OrderRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.0.183:8000")
public class SettlementController {

    private final OrderRepository orderRepository; // JpaRepository<Order, Long>

    @GetMapping("/totals")
    public Map<String, Object> getTotals() {
        int totalPrice = orderRepository.findAll()
                .stream()
                .mapToInt(Order::getTotalPrice)
                .sum();
        return Map.of("totalPrice", totalPrice);
    }
}
