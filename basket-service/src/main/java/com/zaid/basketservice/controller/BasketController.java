package com.zaid.basketservice.controller;

import com.zaid.basketservice.dto.request.AddItemRequest;
import com.zaid.basketservice.dto.request.UpdateItemQuantityRequest;
import com.zaid.basketservice.model.Basket;
import com.zaid.basketservice.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/baskets")
@RequiredArgsConstructor
@Tag(name = "Basket API", description = "Sepet yönetimi uç noktaları")
public class BasketController {

    private final BasketService basketService;

    @GetMapping
    @Operation(summary = "Kullanıcının sepetini getirir")
    public ResponseEntity<Basket> getBasket(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(basketService.getBasket(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Sepete yeni ürün ekler veya mevcut ürünün miktarını artırır")
    public ResponseEntity<Basket> addItemToBasket(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AddItemRequest request) { // Güncellendi
        return ResponseEntity.ok(basketService.addItemToBasket(userId, request));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Sepetten belirli bir ürünü çıkarır")
    public ResponseEntity<Basket> removeItemFromBasket(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer productId) { // String -> Integer
        return ResponseEntity.ok(basketService.removeItemFromBasket(userId, productId));
    }

    @DeleteMapping
    @Operation(summary = "Sepeti tamamen boşaltır")
    public ResponseEntity<Void> clearBasket(@RequestHeader("X-User-Id") String userId) {
        basketService.clearBasket(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Sepetteki ürünün miktarını günceller. Miktar 0 gönderilirse ürünü sepetten siler.")
    public ResponseEntity<Basket> updateItemQuantity(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer productId,
            @RequestBody UpdateItemQuantityRequest request) {

        return ResponseEntity.ok(basketService.updateItemQuantity(userId, productId, request));
    }
}