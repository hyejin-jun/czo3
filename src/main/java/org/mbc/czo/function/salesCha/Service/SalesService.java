package org.mbc.czo.function.salesCha.Service;

import org.mbc.czo.function.cart.domain.Order;
import org.mbc.czo.function.salesCha.DTO.SalesChartDto;
import org.mbc.czo.function.salesCha.Repository.CartOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class SalesService {

    private final CartOrderRepository cartOrderRepository;

    public SalesService(CartOrderRepository cartOrderRepository) {
        this.cartOrderRepository = cartOrderRepository;
    }

    // 월별 통계
    public List<SalesChartDto> getMonthlyStats(int year) {
        List<SalesChartDto> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            // 주문 조회
            LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
            LocalDateTime end = start.withDayOfMonth(start.toLocalDate().lengthOfMonth())
                    .toLocalDate().atTime(23, 59, 59);
            List<Order> orders = cartOrderRepository.findOrdersInPeriod(start, end);

            // 매출/원가
            long totalRevenue = orders.stream()
                    .flatMap(o -> o.getOrderItems() != null ? o.getOrderItems().stream() : Stream.empty())
                    .mapToLong(item -> (long) item.getOrderPrice() * item.getCount())
                    .sum();

            long totalCost = orders.stream()
                    .flatMap(o -> o.getOrderItems() != null ? o.getOrderItems().stream() : Stream.empty())
                    .mapToLong(item -> (long) item.getCostPrice() * item.getCount())
                    .sum();

            long profit = totalRevenue - totalCost;

            // 판관비(배송비)
            Long deliveryFeeSum = cartOrderRepository.sumShippingFeeByMonth(year, month);
            if (deliveryFeeSum == null) deliveryFeeSum = 0L;

            result.add(new SalesChartDto(
                    year + "-" + String.format("%02d", month),
                    totalRevenue,
                    totalCost,
                    profit,
                    deliveryFeeSum
            ));
        }

        return result;
    }

    // 일별 통계
    public List<SalesChartDto> getDailyStats(int year, int month) {
        List<SalesChartDto> result = new ArrayList<>();
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDateTime start = LocalDate.of(year, month, day).atStartOfDay();
            LocalDateTime end = start.withHour(23).withMinute(59).withSecond(59);
            List<Order> orders = cartOrderRepository.findOrdersInPeriod(start, end);

            long totalRevenue = orders.stream()
                    .flatMap(o -> o.getOrderItems() != null ? o.getOrderItems().stream() : Stream.empty())
                    .mapToLong(item -> (long) item.getOrderPrice() * item.getCount())
                    .sum();

            long totalCost = orders.stream()
                    .flatMap(o -> o.getOrderItems() != null ? o.getOrderItems().stream() : Stream.empty())
                    .mapToLong(item -> (long) item.getCostPrice() * item.getCount())
                    .sum();

            long profit = totalRevenue - totalCost;

            Long deliveryFeeSum = cartOrderRepository.sumShippingFeeByDay(year, month, day);
            if (deliveryFeeSum == null) deliveryFeeSum = 0L;

            result.add(new SalesChartDto(
                    year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day),
                    totalRevenue,
                    totalCost,
                    profit,
                    deliveryFeeSum
            ));
        }

        return result;
    }
}