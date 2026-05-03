package com.zaid.paymentservice.client;


import com.zaid.paymentservice.dto.IyzicoPaymentRequest;
import com.zaid.paymentservice.dto.IyzicoPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class IyzicoMockAdapter implements IyzicoPaymentClient {

    @Override
    public IyzicoPaymentResponse pay(IyzicoPaymentRequest request) {
        log.info("Iyzico mock servisine ödeme isteği atılıyor. Tutar: {}", request.getPrice());

        // Simülasyon: Kart numarası "0000" ile bitiyorsa bakiye yetersiz hatası dön
        if (request.getCardNumber() != null && request.getCardNumber().endsWith("0000")) {
            log.warn("Iyzico mock red yanıtı döndü: Yetersiz bakiye simülasyonu.");
            return IyzicoPaymentResponse.builder()
                    .status("failure")
                    .errorCode("ERR-10051")
                    .errorMessage("Yetersiz bakiye")
                    .build();
        }

        // Başarılı senaryo
        log.info("Iyzico mock başarılı yanıt döndü.");
        return IyzicoPaymentResponse.builder()
                .status("success")
                .paymentId("IYZ-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
    }
}