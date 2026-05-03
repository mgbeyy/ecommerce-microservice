package com.zaid.orderservice.controller;

import com.zaid.orderservice.dto.OrderCreateRequest;
import com.zaid.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody OrderCreateRequest request) {

        Long orderId = orderService.createOrder(userId, request.getPaymentInfo());
        return ResponseEntity.ok(orderId);
    }
}