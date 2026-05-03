package com.zaid.paymentservice.service;

import com.ecommerce.common.commands.ProcessPaymentCommand;
import com.zaid.paymentservice.base.AbstractIntegrationTest;
import com.zaid.paymentservice.client.IyzicoPaymentClient;
import com.zaid.paymentservice.config.RabbitMQConfig;
import com.zaid.paymentservice.dto.IyzicoPaymentResponse;
import com.zaid.paymentservice.entity.Payment;
import com.zaid.paymentservice.enums.PaymentStatus;
import com.zaid.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;

class PaymentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    // Dış dünyayı (Iyzico) mockluyoruz
    @MockitoBean
    private IyzicoPaymentClient iyzicoPaymentClient;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        // 1. Iyzico Mock Ayarı (Başarılı dönecek)
        String mockTransactionId = "IYZ-12345";
        Mockito.when(iyzicoPaymentClient.pay(any())).thenReturn(
                IyzicoPaymentResponse.builder().status("success").paymentId(mockTransactionId).build()
        );

        // 2. RabbitMQ'ya Event Gönder
        String orderId = UUID.randomUUID().toString();
        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .totalAmount(BigDecimal.valueOf(100.0))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, command);

        // 3. Asenkron işlemin bitmesini bekle ve DB'yi doğrula (En fazla 5 saniye bekle)
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Payment payment = paymentRepository.findAll().stream().findFirst().orElse(null);
            assertThat(payment).isNotNull();
            assertThat(payment.getOrderId().toString()).isEqualTo(orderId);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(payment.getIyzicoTransactionId()).isEqualTo(mockTransactionId);
        });
    }

    @Test
    void shouldProcessPaymentFailed() {
        // 1. Iyzico Mock Ayarı (Başarısız dönecek)
        Mockito.when(iyzicoPaymentClient.pay(any())).thenReturn(
                IyzicoPaymentResponse.builder().status("failure").errorMessage("Yetersiz Bakiye").build()
        );

        // 2. RabbitMQ'ya Event Gönder
        String orderId = UUID.randomUUID().toString();
        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .totalAmount(BigDecimal.valueOf(5000.0))
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, command);

        // 3. Asenkron işlemin bitmesini bekle ve DB'yi doğrula
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Payment payment = paymentRepository.findAll().stream().findFirst().orElse(null);
            assertThat(payment).isNotNull();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        });
    }

    @Test
    void shouldHandleIdempotencyAndIgnoreDuplicateMessages() {
        // 1. Iyzico Mock Ayarı
        Mockito.when(iyzicoPaymentClient.pay(any())).thenReturn(
                IyzicoPaymentResponse.builder().status("success").paymentId("IYZ-IDEMP").build()
        );

        String orderId = UUID.randomUUID().toString();
        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .totalAmount(BigDecimal.valueOf(200.0))
                .build();

        // 2. Aynı mesajı arka arkaya İKİ KERE gönder (Ağ hatası simülasyonu)
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, command);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, command);

        // 3. İşlemlerin bitmesi için kısa bir süre bekle
        await().pollDelay(Duration.ofSeconds(2)).until(() -> true);

        // 4. Veritabanında Unique Constraint çalıştığı için sadece 1 kayıt olmalı
        long paymentCount = paymentRepository.count();
        assertThat(paymentCount).isEqualTo(1L);

        // 5. Iyzico API'sine (Mock) sadece 1 kere gidildiğini doğrula (Mükerrer çekim engellendi)
        Mockito.verify(iyzicoPaymentClient, Mockito.times(1)).pay(any());
    }
}