package com.zaid.productservice.service;

import com.zaid.productservice.dto.request.ProductCreateRequest;
import com.zaid.productservice.dto.request.ProductUpdateRequest;
import com.zaid.productservice.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);
    void deleteProduct(Long id);
}