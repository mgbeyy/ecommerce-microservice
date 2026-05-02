package com.zaid.productservice.service.impl;

import com.zaid.productservice.dto.request.ProductCreateRequest;
import com.zaid.productservice.dto.request.ProductUpdateRequest;
import com.zaid.productservice.dto.response.ProductResponse;
import com.zaid.productservice.entity.Category;
import com.zaid.productservice.entity.Product;
import com.zaid.productservice.exception.ResourceNotFoundException;
import com.zaid.productservice.repository.ICategoryRepository;
import com.zaid.productservice.repository.IProductRepository;
import com.zaid.productservice.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı. ID: " + request.categoryId()));

        Product product = request.toEntity(category);
        Product savedProduct = productRepository.save(product);

        return ProductResponse.fromEntity(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı. ID: " + id));

        return ProductResponse.fromEntity(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        // IProductRepository içindeki @EntityGraph sayesinde category join fetch ile tek sorguda gelecek
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductResponse::fromEntity);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek ürün bulunamadı. ID: " + id));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı. ID: " + request.categoryId()));

        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setPrice(request.price());
        existingProduct.setStock(request.stock());
        existingProduct.setImageUrl(request.imageUrl());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Silinecek ürün bulunamadı. ID: " + id);
        }
        productRepository.deleteById(id);
    }
}