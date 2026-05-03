package com.zaid.basketservice.dto.request;

import lombok.Data;

@Data
public class AddItemRequest {
    private Integer productId;
    private Integer quantity;
}