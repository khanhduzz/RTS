package com.khanhduzz.tradingsystem.infrastructure.persistence;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.enums.OrderSide;
import com.khanhduzz.tradingsystem.enums.OrderStatus;

import org.springframework.context.event.EventListener;

import java.math.BigDecimal;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class OrderBook {

    private final Map<String, OrderBookEntry> books = new HashMap<>();
    private final OrderRepository orderRepository;

    public OrderBook(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadPendingOrders() {
        System.out.println("Loading pending orders from database...");

        List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
        List<Order> pendingOrders = orderRepository.findByStatusIn(statuses);

        for (Order order : pendingOrders) {
            addOrder(order);
        }
        System.out.println("Loaded " + pendingOrders.size() + " pending orders.");
    }

    public void addOrder(Order order) {
        if (order.getStatus() == OrderStatus.PENDING ||
                order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
            books.computeIfAbsent(order.getSymbol(), k -> new OrderBookEntry())
                    .addOrder(order);
        }
    }

    public void matchOrders(String symbol) {
        OrderBookEntry entry = books.get(symbol);
        if (entry != null) {
            entry.match(orderRepository);
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

    public void match(OrderRepository orderRepository) {
        while (!buys.isEmpty() && !sells.isEmpty()) {
            BigDecimal bestBuyPrice = buys.firstKey();
            BigDecimal bestSellPrice = sells.firstKey();

            // No match possible
            if (bestBuyPrice.compareTo(bestSellPrice) < 0) {
                break;
            }

            Deque<Order> buyQueue = buys.get(bestBuyPrice);
            Deque<Order> sellQueue = sells.get(bestSellPrice);

            Order buyOrder = buyQueue.peekFirst();
            Order sellOrder = sellQueue.peekFirst();

            if (buyOrder == null || sellOrder == null)
                break;

            // Calculate match quantity
            int matchQty = Math.min(
                    buyOrder.getQuantity() - buyOrder.getFilledQuantity(),
                    sellOrder.getQuantity() - sellOrder.getFilledQuantity());

            if (matchQty > 0) {
                // Execute trade
                executeTrade(buyOrder, sellOrder, matchQty, orderRepository);
            }

            // Remove filled orders
            if (buyOrder.getFilledQuantity() >= buyOrder.getQuantity()) {
                buyQueue.pollFirst();
                if (buyQueue.isEmpty())
                    buys.remove(bestBuyPrice);
            }
            if (sellOrder.getFilledQuantity() >= sellOrder.getQuantity()) {
                sellQueue.pollFirst();
                if (sellQueue.isEmpty())
                    sells.remove(bestSellPrice);
            }
        }
    }

    private void executeTrade(Order buyOrder, Order sellOrder, int qty, OrderRepository orderRepository) {
        buyOrder.fill(qty);
        sellOrder.fill(qty);

        orderRepository.save(buyOrder);
        orderRepository.save(sellOrder);

        System.out.println("TRADE EXECUTED: " + qty + " shares of " + buyOrder.getSymbol()
                + " @ " + buyOrder.getPrice()
                + " | Buyer: " + buyOrder.getUserId()
                + " | Seller: " + sellOrder.getUserId());

        // Later we will publish TradeExecuted event here
    }
}