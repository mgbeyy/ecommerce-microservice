package com.zaid.orderservice.client;

import com.zaid.orderservice.config.FeignConfig;
import com.zaid.orderservice.dto.BasketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "basket-service", configuration = FeignConfig.class)
public interface BasketClient {

    @GetMapping("/api/v1/baskets")
    BasketDto getBasket();


    @DeleteMapping("/api/v1/baskets")
    void clearBasket();
}