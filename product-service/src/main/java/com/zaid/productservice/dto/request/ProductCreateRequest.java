package com.zaid.productservice.dto.request;

import com.zaid.productservice.entity.Category;
import com.zaid.productservice.entity.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "Ürün adı boş olamaz")
        String name,

        String description,

        @NotNull(message = "Fiyat boş olamaz")
        @Positive(message = "Fiyat sıfırdan büyük olmalıdır")
        BigDecimal price,

        @NotNull(message = "Stok boş olamaz")
        @Min(value = 0, message = "Stok adedi eksi olamaz")
        Integer stock,

        @NotBlank(message = "Resim URL boş olamaz")
        String imageUrl,

        @NotNull(message = "Kategori ID boş olamaz")
        Long categoryId
) {
    public Product toEntity(Category category) {
        return Product.builder()
                .name(this.name())
                .description(this.description())
                .price(this.price())
                .stock(this.stock())
                .imageUrl(this.imageUrl())
                .category(category)
                .build();
    }
}