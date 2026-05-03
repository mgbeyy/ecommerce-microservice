package com.zaid.basketservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Basket {
    private String userId;
    private Map<Integer, BasketItem> items = new HashMap<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Basket(String userId) {
        this.userId = userId;
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.values().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.updatedAt = Instant.now();
    }
}