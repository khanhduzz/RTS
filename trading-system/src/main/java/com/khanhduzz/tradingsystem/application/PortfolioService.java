package com.khanhduzz.tradingsystem.application;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.domain.portfolio.Holding;
import com.khanhduzz.tradingsystem.domain.portfolio.Portfolio;
import com.khanhduzz.tradingsystem.infrastructure.persistence.PortfolioRepository;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @SuppressWarnings("null")
    @Transactional
    public Portfolio getOrCreatePortfolio(String userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Portfolio portfolio = Portfolio.builder()
                            .userId(userId)
                            .cashBalance(new BigDecimal("100000000")) // 100 triệu VND
                            .build();
                    return portfolioRepository.save(portfolio);
                });
    }

    @Transactional
    public void updateAfterTrade(Order buyOrder, Order sellOrder, int qty, BigDecimal price) {
        // Update Buyer
        Portfolio buyer = getOrCreatePortfolio(buyOrder.getUserId());
        updateBuyerPortfolio(buyer, buyOrder.getSymbol(), qty, price);

        // Update Seller
        Portfolio seller = getOrCreatePortfolio(sellOrder.getUserId());
        updateSellerPortfolio(seller, sellOrder.getSymbol(), qty, price);
    }

    private void updateBuyerPortfolio(Portfolio portfolio, String symbol, int qty, BigDecimal price) {
        portfolio.getHoldings().compute(symbol, (k, holding) -> {
            if (holding == null) {
                return new Holding(qty, price);
            } else {
                // Calculate new average price
                BigDecimal totalCost = holding.getAveragePrice().multiply(new BigDecimal(holding.getQuantity()))
                        .add(price.multiply(new BigDecimal(qty)));
                int newQty = holding.getQuantity() + qty;
                return new Holding(newQty, totalCost.divide(new BigDecimal(newQty), 2, RoundingMode.HALF_UP));
            }
        });

        // Deduct cash
        BigDecimal cost = price.multiply(new BigDecimal(qty));
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(cost));

        portfolioRepository.save(portfolio);
    }

    private void updateSellerPortfolio(Portfolio portfolio, String symbol, int qty, BigDecimal price) {
        Holding holding = portfolio.getHoldings().get(symbol);
        if (holding != null) {
            holding.setQuantity(holding.getQuantity() - qty);
            if (holding.getQuantity() <= 0) {
                portfolio.getHoldings().remove(symbol);
            }
        }

        // Add cash
        BigDecimal revenue = price.multiply(new BigDecimal(qty));
        portfolio.setCashBalance(portfolio.getCashBalance().add(revenue));

        portfolioRepository.save(portfolio);
    }
}