package com.khanhduzz.tradingsystem.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                            .cashBalance(new java.math.BigDecimal("100000000"))
                            .build();
                    return portfolioRepository.save(portfolio);
                });
    }
}