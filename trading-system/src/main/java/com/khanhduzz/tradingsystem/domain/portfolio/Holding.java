package com.khanhduzz.tradingsystem.domain.portfolio;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {
    @Builder.Default
    private Integer quantity = 0;
    @Builder.Default
    private BigDecimal averagePrice = BigDecimal.ZERO;
}
