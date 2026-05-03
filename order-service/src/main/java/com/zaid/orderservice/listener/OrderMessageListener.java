package com.zaid.orderservice.listener;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.ecommerce.common.events.PaymentFailedEvent;
import com.zaid.orderservice.config.RabbitMQConfig;
import com.zaid.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("PaymentCompletedEvent alındı. OrderId: {}", event.getOrderId());
        orderService.completeOrder(Long.valueOf(event.getOrderId()), event.getPaymentTransactionId());
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("PaymentFailedEvent alındı. OrderId: {}, Hata: {}", event.getOrderId(), event.getErrorMessage());
        orderService.cancelOrder(Long.valueOf(event.getOrderId()), event.getErrorMessage());
    }
}