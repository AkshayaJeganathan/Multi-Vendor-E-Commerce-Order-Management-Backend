package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Inventory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class InventoryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 0, message = "Quantity cannot be negative")
        private int quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long productId;
        private String productName;
        private int totalQuantity;
        private int reservedQuantity;
        private int availableQuantity;
        private LocalDateTime lastUpdated;

        public static Response from(Inventory inventory) {
            return Response.builder()
                    .id(inventory.getId())
                    .productId(inventory.getProduct().getId())
                    .productName(inventory.getProduct().getName())
                    .totalQuantity(inventory.getTotalQuantity())
                    .reservedQuantity(inventory.getReservedQuantity())
                    .availableQuantity(inventory.getAvailableQuantity())
                    .lastUpdated(inventory.getLastUpdated())
                    .build();
        }
    }
}
