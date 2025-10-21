package org.mbc.czo.function.cart.repository;

import org.mbc.czo.function.cart.domain.Delivery;
import org.mbc.czo.function.cart.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {


}
