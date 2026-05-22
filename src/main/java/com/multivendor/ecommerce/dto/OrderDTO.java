package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        private String shippingAddress;

        @NotEmpty(message = "Order must have at least one item")
        private List<OrderItemRequest> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String orderNumber;
        private Long customerId;
        private String customerName;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private LocalDateTime placedAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private List<OrderItemResponse> items;

        public static Response from(Order order) {
            List<OrderItemResponse> items = order.getOrderItems() == null ? List.of() :
                    order.getOrderItems().stream()
                            .map(OrderItemResponse::from)
                            .collect(Collectors.toList());
            return Response.builder()
                    .id(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomer().getId())
                    .customerName(order.getCustomer().getName())
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .shippingAddress(order.getShippingAddress())
                    .placedAt(order.getPlacedAt())
                    .shippedAt(order.getShippedAt())
                    .deliveredAt(order.getDeliveredAt())
                    .items(items)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Long sellerId;
        private String sellerName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .sellerId(item.getSeller().getId())
                    .sellerName(item.getSeller().getName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "Status is required")
        private Order.OrderStatus status;
    }
}
