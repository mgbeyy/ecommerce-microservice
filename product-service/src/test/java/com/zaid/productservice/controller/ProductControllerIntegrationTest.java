package com.zaid.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaid.productservice.dto.request.ProductCreateRequest;
import com.zaid.productservice.dto.response.ProductResponse;
import com.zaid.productservice.entity.Category;
import com.zaid.productservice.exception.ErrorResponse;
import com.zaid.productservice.repository.ICategoryRepository;
import com.zaid.productservice.repository.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

// Sunucuyu test ortamında gerçek bir portta ayağa kaldırır
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    // Spring'in ayağa kaldırdığı rastgele portu yakalıyoruz
    @Value("${local.server.port}")
    private int port;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long defaultCategoryId;

    // Saf Java 11+ HttpClient (Hiçbir Spring web bağımlılığına ihtiyaç duymaz)
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = Category.builder()
                .name("Test Kategorisi")
                .build();
        category = categoryRepository.save(category);
        defaultCategoryId = category.getId();
    }

    @Test
    void createProduct_WithValidRequest_ShouldReturnCreatedAndProductData() throws Exception {
        // Arrange
        ProductCreateRequest requestDto = new ProductCreateRequest(
                "PlayStation 5", "Oyun Konsolu", BigDecimal.valueOf(20000), 15, "ps5.jpg", defaultCategoryId
        );
        String requestBody = objectMapper.writeValueAsString(requestDto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/products"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", "101")
                .header("Trace-Id", "trace-123")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Act
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(201, response.statusCode());
        ProductResponse responseObj = objectMapper.readValue(response.body(), ProductResponse.class);
        assertNotNull(responseObj);
        assertEquals("PlayStation 5", responseObj.name());
        assertEquals("Test Kategorisi", responseObj.categoryName());
    }

    @Test
    void createProduct_WithInvalidRequest_ShouldReturnBadRequestAndCustomErrorSchema() throws Exception {
        // Arrange - Fiyat ve stok negatif, ad boş
        ProductCreateRequest requestDto = new ProductCreateRequest(
                "", "Hatalı Ürün", BigDecimal.valueOf(-100), -5, "", defaultCategoryId
        );
        String requestBody = objectMapper.writeValueAsString(requestDto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/products"))
                .header("Content-Type", "application/json")
                .header("Trace-Id", "trace-err-456")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Act
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(400, response.statusCode());
        ErrorResponse responseObj = objectMapper.readValue(response.body(), ErrorResponse.class);
        assertEquals(400, responseObj.status());
        assertEquals("Bad Request", responseObj.error());
        assertEquals("/api/v1/products", responseObj.path());
        assertEquals("trace-err-456", responseObj.traceId());
        assertTrue(responseObj.message().contains("boş olamaz"));
    }

    @Test
    void getProductById_WhenNotFound_ShouldReturn404AndCustomErrorSchema() throws Exception {
        // Arrange
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/products/9999"))
                .header("Content-Type", "application/json")
                .header("Trace-Id", "trace-404")
                .GET()
                .build();

        // Act
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Assert
        assertEquals(404, response.statusCode());
        ErrorResponse responseObj = objectMapper.readValue(response.body(), ErrorResponse.class);
        assertEquals(404, responseObj.status());
        assertEquals("Not Found", responseObj.error());
        assertEquals("Ürün bulunamadı. ID: 9999", responseObj.message());
        assertEquals("trace-404", responseObj.traceId());
    }
}