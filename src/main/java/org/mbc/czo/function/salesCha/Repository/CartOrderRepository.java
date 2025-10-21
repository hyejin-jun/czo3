package org.mbc.czo.function.salesCha.Repository;

import org.mbc.czo.function.cart.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CartOrderRepository extends JpaRepository<Order, Long> {
    // 기간별 주문 조회
    // 특정 기간 주문 조회
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    List<Order> findOrdersInPeriod(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.shippingFee) FROM Order o " +
            "WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month")
    Long sumShippingFeeByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(o.shippingFee) FROM Order o " +
            "WHERE YEAR(o.orderDate) = :year AND MONTH(o.orderDate) = :month AND DAY(o.orderDate) = :day")
    Long sumShippingFeeByDay(@Param("year") int year, @Param("month") int month, @Param("day") int day);


}

