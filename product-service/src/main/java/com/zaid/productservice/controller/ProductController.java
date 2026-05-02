package com.zaid.productservice.controller;

import com.zaid.productservice.dto.request.ProductCreateRequest;
import com.zaid.productservice.dto.request.ProductUpdateRequest;
import com.zaid.productservice.dto.response.ProductResponse;
import com.zaid.productservice.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product API", description = "Ürün yönetimi için uç noktalar")
public class ProductController {

    private final IProductService productService;

    @PostMapping
    @Operation(summary = "Yeni ürün ekle", description = "Sisteme yeni bir ürün kaydeder.")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Yeni ürün oluşturma isteği alındı: {}", request.name());
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID'ye göre ürün getir", description = "Belirtilen ID'ye sahip ürünün detaylarını döner.")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Ürünleri listele", description = "Sayfalanmış ve sıralanmış şekilde ürün listesini döner.")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ürün güncelle", description = "Mevcut bir ürünün bilgilerini günceller.")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Ürün güncelleme isteği alındı. ID: {}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil", description = "Belirtilen ID'ye sahip ürünü sistemden siler.")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Ürün silme isteği alındı. ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}