package com.zaid.paymentservice.listener;

import com.ecommerce.common.commands.ProcessPaymentCommand;
import com.zaid.paymentservice.config.RabbitMQConfig;
import com.zaid.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentRequest(@Payload ProcessPaymentCommand command,
                                     @Header(value = "Trace-Id", required = false) String traceId) {
        // Trace ID yoksa loglarda belli olması için varsayılan bir değer atayabiliriz
        if (traceId != null) {
            MDC.put("Trace-Id", traceId);
        } else {
            MDC.put("Trace-Id", "NO-TRACE-ID");
        }

        try {
            log.info("RabbitMQ'dan ödeme komutu alındı. OrderId: {}", command.getOrderId());
            paymentService.processPayment(command);
        } finally {
            // Memory leak veya log karışıklığını önlemek için işlemi bitirince MDC'yi temizliyoruz
            MDC.clear();
        }
    }
}