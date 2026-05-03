package com.zaid.paymentservice.client;


import com.zaid.paymentservice.dto.IyzicoPaymentRequest;
import com.zaid.paymentservice.dto.IyzicoPaymentResponse;

public interface IyzicoPaymentClient {
    IyzicoPaymentResponse pay(IyzicoPaymentRequest request);
}