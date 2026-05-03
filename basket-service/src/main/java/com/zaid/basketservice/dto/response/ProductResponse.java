package com.zaid.basketservice.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Integer id;
    private BigDecimal price; // JSON'daki "price" ile eşleşmeli
}