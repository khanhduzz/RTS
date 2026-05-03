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
                executeTrade(buyOrder, sellOrder, matchQty);
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

    private void executeTrade(Order buyOrder, Order sellOrder, int qty) {
        buyOrder.fill(qty);
        sellOrder.fill(qty);

        System.out.println("TRADE EXECUTED: " + qty + " shares of " + buyOrder.getSymbol()
                + " @ " + buyOrder.getPrice()
                + " | Buyer: " + buyOrder.getUserId()
                + " | Seller: " + sellOrder.getUserId());

        // Later we will publish TradeExecuted event here
    }
}