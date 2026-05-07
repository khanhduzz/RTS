package com.khanhduzz.tradingsystem.application;

import com.khanhduzz.tradingsystem.domain.order.Order;
import com.khanhduzz.tradingsystem.domain.portfolio.Holding;
import com.khanhduzz.tradingsystem.domain.portfolio.Portfolio;
import com.khanhduzz.tradingsystem.infrastructure.persistence.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
                            .cashBalance(new BigDecimal("100000000"))
                            .build();
                    return portfolioRepository.save(portfolio);
                });
    }

    @Transactional
    public void updateAfterTrade(Order buyOrder, Order sellOrder, int qty, BigDecimal price) {
        updateBuyer(buyOrder.getUserId(), buyOrder.getSymbol(), qty, price);
        updateSeller(sellOrder.getUserId(), sellOrder.getSymbol(), qty, price);
    }

    private void updateBuyer(String userId, String symbol, int qty, BigDecimal price) {
        Portfolio portfolio = getOrCreatePortfolio(userId);

        portfolio.getHoldings().compute(symbol, (k, holding) -> {
            if (holding == null) {
                return Holding.builder()
                        .quantity(qty)
                        .averagePrice(price)
                        .build();
            }
            // Calculate new average price
            BigDecimal totalCost = holding.getAveragePrice()
                    .multiply(BigDecimal.valueOf(holding.getQuantity()))
                    .add(price.multiply(BigDecimal.valueOf(qty)));

            int newQty = holding.getQuantity() + qty;
            BigDecimal newAvg = totalCost.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP);

            return Holding.builder()
                    .quantity(newQty)
                    .averagePrice(newAvg)
                    .build();
        });

        BigDecimal cost = price.multiply(BigDecimal.valueOf(qty));
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(cost));

        portfolioRepository.save(portfolio);
    }

    private void updateSeller(String userId, String symbol, int qty, BigDecimal price) {
        Portfolio portfolio = getOrCreatePortfolio(userId);

        portfolio.getHoldings().computeIfPresent(symbol, (k, holding) -> {
            int newQty = holding.getQuantity() - qty;
            if (newQty <= 0) {
                return null; // Remove from map
            }
            return Holding.builder()
                    .quantity(newQty)
                    .averagePrice(holding.getAveragePrice())
                    .build();
        });

        BigDecimal revenue = price.multiply(BigDecimal.valueOf(qty));
        portfolio.setCashBalance(portfolio.getCashBalance().add(revenue));

        portfolioRepository.save(portfolio);
    }
}