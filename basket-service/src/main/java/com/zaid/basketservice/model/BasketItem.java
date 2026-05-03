package com.zaid.basketservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem {
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}