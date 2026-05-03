package com.khanhduzz.tradingsystem.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.khanhduzz.tradingsystem.application.PortfolioService;
import com.khanhduzz.tradingsystem.domain.portfolio.Portfolio;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public Portfolio getPortfolio(@PathVariable String userId) {
        return portfolioService.getOrCreatePortfolio(userId);
    }
}