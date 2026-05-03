package com.khanhduzz.tradingsystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(String userId);

    List<Order> findBySymbol(String symbol);

    List<Order> findByStatus(OrderStatus status);
}