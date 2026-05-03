package com.khanhduzz.tradingsystem.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khanhduzz.tradingsystem.domain.portfolio.Portfolio;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, String> {
    Optional<Portfolio> findByUserId(String userId);
}