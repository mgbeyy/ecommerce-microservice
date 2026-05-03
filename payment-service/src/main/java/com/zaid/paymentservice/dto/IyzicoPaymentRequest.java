package com.zaid.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IyzicoPaymentRequest {
    private String cardNumber;
    private String expireYear;
    private String expireMonth;
    private String cvc;
    private BigDecimal price;
    private String currency; // Örn: TRY
}