package com.khanhduzz.tradingsystem.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;

import com.khanhduzz.tradingsystem.enums.OrderSide;
import com.khanhduzz.tradingsystem.enums.OrderType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String symbol;

    private OrderSide side;
    private OrderType type;

    private BigDecimal price;

    @Positive
    private Integer quantity;
}