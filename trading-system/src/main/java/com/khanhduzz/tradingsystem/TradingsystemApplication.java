package com.khanhduzz.tradingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.khanhduzz.tradingsystem.infrastructure.persistence")
@EntityScan(basePackages = "com.khanhduzz.tradingsystem.domain")
public class TradingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingsystemApplication.class, args);
	}

}
