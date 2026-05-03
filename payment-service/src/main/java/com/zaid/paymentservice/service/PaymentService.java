package com.zaid.paymentservice.service;


import com.ecommerce.common.commands.ProcessPaymentCommand;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.events.PaymentFailedEvent;
import com.zaid.paymentservice.client.IyzicoPaymentClient;
import com.zaid.paymentservice.config.RabbitMQConfig;
import com.zaid.paymentservice.dto.IyzicoPaymentRequest;
import com.zaid.paymentservice.dto.IyzicoPaymentResponse;
import com.zaid.paymentservice.entity.Payment;
import com.zaid.paymentservice.enums.PaymentStatus;
import com.zaid.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IyzicoPaymentClient iyzicoPaymentClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void processPayment(ProcessPaymentCommand command) {
        log.info("Ödeme işlemi başlatılıyor. OrderId: {}", command.getOrderId());

        try {
            // 1. Veritabanına kaydet (Başlangıçta PENDING / FAILED kaydetmiyoruz, duruma göre kaydedeceğiz.
            // Ancak Idempotency için önce DB'ye yazmak önemli)
            Payment payment = Payment.builder()
                    .orderId(command.getOrderId())
                    .userId(command.getCustomerId())
                    .amount(command.getTotalAmount())
                    .status(PaymentStatus.FAILED) // Varsayılan FAILED, başarılı olursa güncelleyeceğiz
                    .build();

            // Idempotency: Eğer bu order_id zaten varsa DataIntegrityViolationException fırlatır.
            payment = paymentRepository.saveAndFlush(payment);

            // 2. Iyzico Mock Servisini Çağır
            IyzicoPaymentRequest request = IyzicoPaymentRequest.builder()
                    .cardNumber(command.getCardNumber())
                    .expireMonth(command.getExpireMonth())
                    .expireYear(command.getExpireYear())
                    .cvc(command.getCvc())
                    .price(command.getTotalAmount())
                    .currency("TRY")
                    .build();

            IyzicoPaymentResponse response = iyzicoPaymentClient.pay(request);

            // 3. Yanıta Göre İşlem Yap
            if ("success".equals(response.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setIyzicoTransactionId(response.getPaymentId());
                paymentRepository.save(payment);

                publishSuccessEvent(command.getOrderId(), response.getPaymentId());
                log.info("Ödeme başarılı. OrderId: {}, TransactionId: {}", command.getOrderId(), response.getPaymentId());
            } else {
                publishFailedEvent(command.getOrderId(), response.getErrorMessage());
                log.warn("Ödeme reddedildi. OrderId: {}, Hata: {}", command.getOrderId(), response.getErrorMessage());
            }

        } catch (DataIntegrityViolationException e) {
            // Idempotency durumu: Bu orderId için zaten işlem yapılıyor/yapılmış. Mesajı yutuyoruz.
            log.warn("Idempotency uyarısı: OrderId {} için zaten ödeme mevcut. Mesaj atlanıyor.", command.getOrderId());
        } catch (Exception e) {
            log.error("Ödeme sırasında beklenmeyen hata oluştu. OrderId: {}", command.getOrderId(), e);
            publishFailedEvent(command.getOrderId(), "Sistem hatası: " + e.getMessage());
        }
    }

    private void publishSuccessEvent(Long orderId, String transactionId) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(orderId)
                .paymentTransactionId(transactionId) // Senin belirttiğin doğru alan adı
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_PAYMENT_SUCCESS_ROUTING_KEY, event);
    }

    private void publishFailedEvent(Long orderId, String errorMessage) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(orderId)
                .errorMessage(errorMessage)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_PAYMENT_FAILED_ROUTING_KEY, event);
    }
}