package com.zaid.productservice.service.impl;

import com.zaid.productservice.dto.request.ProductCreateRequest;
import com.zaid.productservice.dto.response.ProductResponse;
import com.zaid.productservice.entity.Category;
import com.zaid.productservice.entity.Product;
import com.zaid.productservice.exception.ResourceNotFoundException;
import com.zaid.productservice.repository.ICategoryRepository;
import com.zaid.productservice.repository.IProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_WhenCategoryExists_ShouldReturnProductResponse() {
        // Arrange (Manuel Test Verisi)
        Long categoryId = 1L;
        Category mockCategory = Category.builder()
                .id(categoryId)
                .name("Elektronik")
                .build();

        ProductCreateRequest request = new ProductCreateRequest(
                "Telefon", "Akıllı Telefon", BigDecimal.valueOf(10000), 10, "url", categoryId
        );

        Product savedProduct = Product.builder()
                .id(100L)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .category(mockCategory)
                .build();
        savedProduct.setCreatedAt(LocalDateTime.now());
        savedProduct.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Telefon", response.name());
        assertEquals("Elektronik", response.categoryName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WhenCategoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long invalidCategoryId = 99L;
        ProductCreateRequest request = new ProductCreateRequest(
                "Telefon", "Akıllı Telefon", BigDecimal.valueOf(10000), 10, "url", invalidCategoryId
        );

        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createProduct(request)
        );

        assertTrue(exception.getMessage().contains("Kategori bulunamadı"));
        verify(categoryRepository, times(1)).findById(invalidCategoryId);
        verify(productRepository, never()).save(any(Product.class)); // Hata fırladığı için save metoduna hiç girilmemeli
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProductResponse() {
        // Arrange
        Long productId = 1L;
        Category mockCategory = Category.builder().id(1L).name("Elektronik").build();
        Product mockProduct = Product.builder()
                .id(productId)
                .name("Laptop")
                .price(BigDecimal.valueOf(25000))
                .stock(5)
                .category(mockCategory)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Act
        ProductResponse response = productService.getProductById(productId);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", response.name());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteSuccessfully() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // Assert
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long productId = 99L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId)
        );

        assertTrue(exception.getMessage().contains("Silinecek ürün bulunamadı"));
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(anyLong());
    }
}