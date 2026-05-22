package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull(message = "Seller ID is required")
        private Long sellerId;

        @NotBlank(message = "Product name is required")
        private String name;

        private String description;
        private String category;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        private BigDecimal mrp;
        private String sku;
        private int initialStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long sellerId;
        private String sellerName;
        private String name;
        private String description;
        private String category;
        private BigDecimal price;
        private BigDecimal mrp;
        private String sku;
        private Product.ProductStatus status;
        private Integer availableQuantity;
        private LocalDateTime createdAt;

        public static Response from(Product product) {
            return Response.builder()
                    .id(product.getId())
                    .sellerId(product.getSeller().getId())
                    .sellerName(product.getSeller().getName())
                    .name(product.getName())
                    .description(product.getDescription())
                    .category(product.getCategory())
                    .price(product.getPrice())
                    .mrp(product.getMrp())
                    .sku(product.getSku())
                    .status(product.getStatus())
                    .createdAt(product.getCreatedAt())
                    .build();
        }
    }
}
