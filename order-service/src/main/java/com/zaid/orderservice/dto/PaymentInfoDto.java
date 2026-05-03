package com.zaid.orderservice.dto;

import lombok.Data;

@Data
public class PaymentInfoDto {
    private String cardNumber;
    private String expireMonth;
    private String expireYear;
    private String cvc;
}