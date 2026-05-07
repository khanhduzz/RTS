package com.khanhduzz.tradingsystem.domain.portfolio;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    private String userId;

    @ElementCollection
    @CollectionTable(name = "portfolio_holdings", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "symbol")
    // @Column(name = "holding")
    @Builder.Default
    private Map<String, Holding> holdings = new HashMap<>();

    @Builder.Default
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal cashBalance = BigDecimal.ZERO;
}
