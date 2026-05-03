package com.ecommerce.common.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentCommand implements Serializable {
    private Long orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    // MVP için kart bilgileri
    private String cardNumber;
    private String expireMonth;
    private String expireYear;
    private String cvc;
}