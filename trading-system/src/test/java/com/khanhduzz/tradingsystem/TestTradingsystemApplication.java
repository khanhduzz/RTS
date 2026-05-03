package com.khanhduzz.tradingsystem;

import org.springframework.boot.SpringApplication;

public class TestTradingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(TradingsystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
