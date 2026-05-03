package com.khanhduzz.tradingsystem.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.khanhduzz.tradingsystem.infrastructure.persistence.OrderBook;

@Configuration
public class AppConfig {

    @Bean
    public OrderBook orderBook() {
        return new OrderBook();
    }
}