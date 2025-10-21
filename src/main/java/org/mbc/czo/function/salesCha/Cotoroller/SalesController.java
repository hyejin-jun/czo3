package org.mbc.czo.function.salesCha.Cotoroller;

import lombok.RequiredArgsConstructor;
import org.mbc.czo.function.Salary.Entity.Salary;
import org.mbc.czo.function.Salary.Repository.SalaryRepository;
import org.mbc.czo.function.cart.domain.Order;
import org.mbc.czo.function.cart.repository.OrderRepository;
import org.mbc.czo.function.salesCha.DTO.SalesChartDto;
import org.mbc.czo.function.salesCha.Service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://192.168.0.183:3000")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final OrderRepository orderRepository;
    private final SalaryRepository salaryRepository;

    // 명시적 생성자 제거! Lombok이 자동 생성




    @GetMapping("/total")
    public Map<String, Object> getTotalSales() {
        long total = orderRepository.findAll()
                .stream()
                .mapToLong(Order::getTotalPrice) // Order 엔티티에 totalPrice getter 필요
                .sum();
        return Map.of("totalSales", total);
    }

    @GetMapping("/monthly")
    public List<SalesChartDto> getMonthly(@RequestParam int year) {
        return salesService.getMonthlyStats(year);
    }

    @GetMapping("/daily")
    public List<SalesChartDto> getDaily(@RequestParam int year, @RequestParam int month) {
        return salesService.getDailyStats(year, month);
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> getSales(
            @PathVariable("type") String type,
            @RequestParam("year") int year,
            @RequestParam(value = "month", required = false) Integer month) {

        List<Order> allOrders = orderRepository.findAll();

        Map<String, Long> revenueMap = new TreeMap<>();
        Map<String, Long> costMap = new TreeMap<>();
        Map<String, Long> profitMap = new TreeMap<>();

        for (Order order : allOrders) {
            LocalDateTime date = order.getOrderDate();
            if (date.getYear() != year) continue;

            String periodKey;
            if ("monthly".equals(type)) {
                periodKey = String.format("%d-%02d", date.getYear(), date.getMonthValue());
            } else { // daily
                if (month != null && date.getMonthValue() != month) continue;
                periodKey = String.format("%d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            }

            long revenue = order.getTotalPrice();
            long cost = order.getOrderItems().stream()
                    .mapToLong(item -> (long) item.getCostPrice() * item.getCount()) // price로 대체
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
            map.put("totalRevenue", revenueMap.get(period));
            map.put("totalCost", costMap.get(period));
            map.put("profit", profitMap.get(period));
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }



}