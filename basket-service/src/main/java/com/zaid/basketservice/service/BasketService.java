package com.zaid.basketservice.service;

import com.zaid.basketservice.client.ProductServiceClient;
import com.zaid.basketservice.dto.request.AddItemRequest;
import com.zaid.basketservice.dto.request.UpdateItemQuantityRequest;
import com.zaid.basketservice.dto.response.ProductResponse;
import com.zaid.basketservice.model.Basket;
import com.zaid.basketservice.model.BasketItem;
import com.zaid.basketservice.repository.BasketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductServiceClient productServiceClient; // Eklendi

    public Basket getBasket(String userId) {
        Basket basket = basketRepository.getBasket(userId);
        if (basket == null) {
            basket = new Basket(userId);
            basketRepository.saveBasket(basket);
        }
        return basket;
    }

    public Basket addItemToBasket(String userId, AddItemRequest request) { // Parametre değişti

        // 1. Ürün doğrulaması ve fiyat çekimi (Senkron Çağrı)
        // Eğer ürün bulunamazsa Feign, FeignException (örn: 404) fırlatır. Global Exception Handler'ında bunu yönetebilirsin.
        ProductResponse product = productServiceClient.getProductById(request.getProductId());

        Basket basket = getBasket(userId);

        // 2. Sepete ekleme veya güncelleme
        if (basket.getItems().containsKey(request.getProductId())) {
            BasketItem existingItem = basket.getItems().get(request.getProductId());
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setUnitPrice(product.getPrice()); // Her eklemede güncel fiyatı çekiyoruz (Best Practice)
        } else {
            BasketItem newItem = new BasketItem(request.getProductId(), request.getQuantity(), product.getPrice());
            basket.getItems().put(request.getProductId(), newItem);
        }

        basket.calculateTotalPrice();
        basketRepository.saveBasket(basket);
        return basket;
    }

    public Basket removeItemFromBasket(String userId, Integer productId) { // String -> Integer
        Basket basket = getBasket(userId);
        basket.getItems().remove(productId);
        basket.calculateTotalPrice();
        basketRepository.saveBasket(basket);
        return basket;
    }

    public void clearBasket(String userId) {
        basketRepository.deleteBasket(userId);
    }


    public Basket updateItemQuantity(String userId, Integer productId, UpdateItemQuantityRequest request) {
        Basket basket = getBasket(userId);

        // Eğer ürün sepette varsa işlem yap
        if (basket.getItems().containsKey(productId)) {
            if (request.getQuantity() <= 0) {
                // Kural 2: Miktar 0 veya altına düşerse ürünü sepetten tamamen çıkar
                basket.getItems().remove(productId);
            } else {
                // Kural 1: Idempotent güncelleme (Nihai miktarı set et)
                BasketItem existingItem = basket.getItems().get(productId);
                existingItem.setQuantity(request.getQuantity());
            }

            // Sepet tutarını yeniden hesapla ve kaydet
            basket.calculateTotalPrice();
            basketRepository.saveBasket(basket);
        }

        return basket;
    }
}