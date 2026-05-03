package com.zaid.basketservice.client;

import com.zaid.basketservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// name = "product-service" -> Eureka'da kayıtlı olan isim
// path = "/api/v1/products" -> İstek atılacak baz endpoint
@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable("id") Integer id);
}