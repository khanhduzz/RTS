package com.khanhduzz.tradingsystem.domain.order;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.khanhduzz.tradingsystem.enums.OrderSide;
import com.khanhduzz.tradingsystem.enums.OrderStatus;
import com.khanhduzz.tradingsystem.enums.OrderType;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private BigDecimal price;
    private Integer quantity;

    @Builder.Default
    private Integer filledQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public void fill(int qty) {
        this.filledQuantity += qty;
        if (this.filledQuantity >= this.quantity) {
            this.status = OrderStatus.FILLED;
        } else {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
