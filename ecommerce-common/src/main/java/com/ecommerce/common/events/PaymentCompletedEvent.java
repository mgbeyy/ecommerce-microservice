package com.ecommerce.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {
    private String orderId;
    private String paymentTransactionId; // Iyzico'dan dönen referans kodu
}