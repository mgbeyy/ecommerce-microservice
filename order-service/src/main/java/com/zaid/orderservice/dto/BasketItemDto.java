package com.zaid.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BasketItemDto {
    private Long productId;
    private Integer quantity;

    @JsonProperty("unitPrice")
    private BigDecimal price;
}