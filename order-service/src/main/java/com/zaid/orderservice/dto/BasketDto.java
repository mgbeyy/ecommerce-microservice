package com.zaid.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class BasketDto {
    private String basketId;
    private Long userId;
    private BigDecimal totalPrice;
    private Map<String, BasketItemDto> items;
}