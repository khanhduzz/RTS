package com.khanhduzz.tradingsystem.api.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.khanhduzz.tradingsystem.application.TradingService;
import com.khanhduzz.tradingsystem.application.dto.OrderRequest;
import com.khanhduzz.tradingsystem.domain.order.Order;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final TradingService tradingService;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody @NonNull OrderRequest request) {
        Order order = tradingService.placeOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userId) {
        // TODO: Implement
        return ResponseEntity.ok(List.of());
    }
}