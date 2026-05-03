package com.khanhduzz.tradingsystem.application;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khanhduzz.tradingsystem.application.dto.OrderRequest;
import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.infrastructure.persistence.OrderBook;
import com.khanhduzz.tradingsystem.infrastructure.persistence.OrderRepository;

@Service
@RequiredArgsConstructor
public class TradingService {

    private final OrderRepository orderRepository;
    private final OrderBook orderBook;
    // private final PortfolioService portfolioService;

    @SuppressWarnings("null")
    @Transactional
    public Order placeOrder(OrderRequest request) {
        // Save to DB
        Order order = Order.builder()
                .userId(request.getUserId())
                .symbol(request.getSymbol())
                .side(request.getSide())
                .type(request.getType())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        Order savedOrder = orderRepository.save(order);

        // Add to in-memory order book and try matching
        orderBook.addOrder(savedOrder);
        orderBook.matchOrders(savedOrder.getSymbol());

        return savedOrder;
    }
}