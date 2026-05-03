package com.zaid.orderservice.service;

import com.ecommerce.common.commands.ProcessPaymentCommand;
import com.zaid.orderservice.client.BasketClient;
import com.zaid.orderservice.dto.BasketDto;
import com.zaid.orderservice.dto.PaymentInfoDto;
import com.zaid.orderservice.entity.Order;
import com.zaid.orderservice.entity.OrderItem;
import com.zaid.orderservice.entity.enums.OrderStatus;
import com.zaid.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketClient basketClient;
    private final OrderStateManager stateManager;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Long createOrder(Long userId, PaymentInfoDto paymentInfo) {
        // 1. Sepeti Getir (Senkron)
        BasketDto basket = basketClient.getBasket();
        if (basket == null || basket.getItems().isEmpty()) {
            throw new IllegalStateException("Sepet boş, sipariş oluşturulamaz.");
        }

        // 2. Siparişi PENDING olarak DB'ye kaydet
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(basket.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        basket.getItems().values().forEach(item -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build();
            order.addItem(orderItem);
        });

        orderRepository.save(order);
        log.info("Sipariş oluşturuldu (PENDING). OrderId: {}", order.getId());

        // 3. Sepeti Temizle (Senkron - MVP için)
        try {
            basketClient.clearBasket();
        } catch (Exception e) {
            log.error("Sepet temizlenirken hata oluştu. UserId: {}", userId, e);
            // Sepet temizlenemese bile sipariş sürecini kesmiyoruz (best-effort)
        }

        // 4. Ödeme servisine asenkron komut gönder
        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand();
        paymentCommand.setOrderId(order.getId());
        paymentCommand.setCustomerId(userId);
        paymentCommand.setTotalAmount(order.getTotalAmount());
        paymentCommand.setCardNumber(paymentInfo.getCardNumber());
        paymentCommand.setExpireMonth(paymentInfo.getExpireMonth());
        paymentCommand.setExpireYear(paymentInfo.getExpireYear());
        paymentCommand.setCvc(paymentInfo.getCvc());

        rabbitTemplate.convertAndSend("ecommerce.exchange", "payment.routing.key", paymentCommand);
        log.info("ProcessPaymentCommand RabbitMQ'ya gönderildi. OrderId: {}", order.getId());

        // 5. Sipariş durumunu PAYMENT_WAITING yap
        stateManager.transitionTo(order, OrderStatus.PAYMENT_WAITING);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public void completeOrder(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı"));

        stateManager.transitionTo(order, OrderStatus.COMPLETED);
        // transactionId kaydedilebilir (MVP'de entity'de yoksa logluyoruz)
        log.info("Sipariş COMPLETED durumuna çekildi. OrderId: {}, TransactionId: {}", orderId, transactionId);
    }

    @Transactional
    public void cancelOrder(Long orderId, String errorMessage) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Sipariş bulunamadı"));

        stateManager.transitionTo(order, OrderStatus.CANCELLED);
        log.warn("Sipariş CANCELLED durumuna çekildi. OrderId: {}, Hata: {}", orderId, errorMessage);
    }
}