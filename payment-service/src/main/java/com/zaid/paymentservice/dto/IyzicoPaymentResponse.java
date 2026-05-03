package com.zaid.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IyzicoPaymentResponse {
    private String status; // "success" veya "failure"
    private String errorCode;
    private String errorMessage;
    private String paymentId; // Iyzico'nun döneceği transaction id
}