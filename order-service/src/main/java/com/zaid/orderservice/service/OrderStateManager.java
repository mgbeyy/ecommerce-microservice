package com.zaid.orderservice.service;

import com.zaid.orderservice.entity.Order;
import com.zaid.orderservice.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStateManager {

    public void transitionTo(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();

        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PAYMENT_WAITING || newStatus == OrderStatus.CANCELLED;
            case PAYMENT_WAITING -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };

        if (!valid) {
            throw new IllegalStateException("Geçersiz durum geçişi: " + currentStatus + " -> " + newStatus);
        }

        order.setStatus(newStatus);
    }
}