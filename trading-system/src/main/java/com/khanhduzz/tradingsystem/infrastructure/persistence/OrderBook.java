package com.khanhduzz.tradingsystem.infrastructure.persistence;

import org.springframework.stereotype.Component;
import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.enums.OrderSide;

import java.math.BigDecimal;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class OrderBook {

    private final Map<String, OrderBookEntry> books = new HashMap<>();

    public void addOrder(Order order) {
        books.computeIfAbsent(order.getSymbol(), k -> new OrderBookEntry())
                .addOrder(order);
    }

    public void matchOrders(String symbol) {
        OrderBookEntry entry = books.get(symbol);
        if (entry != null) {
            entry.match();
        }
    }
}

// Inner class for each symbol's book
class OrderBookEntry {
    private final ConcurrentSkipListMap<BigDecimal, Deque<Order>> buys = new ConcurrentSkipListMap<>(
            Comparator.reverseOrder());
    private final ConcurrentSkipListMap<BigDecimal, Deque<Order>> sells = new ConcurrentSkipListMap<>();

    public void addOrder(Order order) {
        var map = order.getSide() == OrderSide.BUY ? buys : sells;
        map.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>()).add(order);
    }

    public void match() {
        // Simple matching logic (will expand later)
    }
}