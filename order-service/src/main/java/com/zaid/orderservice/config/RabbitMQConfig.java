package com.zaid.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ecommerce.exchange";

    // Order Service'in dinleyeceği kuyruklar
    public static final String ORDER_PAYMENT_SUCCESS_QUEUE = "order.payment.success.queue";
    public static final String ORDER_PAYMENT_FAILED_QUEUE = "order.payment.failed.queue";

    public static final String ORDER_PAYMENT_SUCCESS_ROUTING_KEY = "order.payment.success.routing.key";
    public static final String ORDER_PAYMENT_FAILED_ROUTING_KEY = "order.payment.failed.routing.key";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(ORDER_PAYMENT_SUCCESS_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(ORDER_PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Binding successBinding(Queue paymentSuccessQueue, DirectExchange exchange) {
        return BindingBuilder.bind(paymentSuccessQueue).to(exchange).with(ORDER_PAYMENT_SUCCESS_ROUTING_KEY);
    }

    @Bean
    public Binding failedBinding(Queue paymentFailedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(exchange).with(ORDER_PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}