package com.zaid.orderservice.dto;

import lombok.Data;

@Data
public class OrderCreateRequest {
    private PaymentInfoDto paymentInfo;
}