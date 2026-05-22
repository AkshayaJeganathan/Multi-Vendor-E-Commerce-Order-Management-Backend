package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Return;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class ReturnDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Request {
        @NotNull(message = "Order ID is required")
        private Long orderId;

        @NotNull(message = "Order Item ID is required")
        private Long orderItemId;

        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long orderId;
        private String orderNumber;
        private Long orderItemId;
        private String productName;
        private String reason;
        private Return.ReturnStatus status;
        private LocalDateTime requestedAt;
        private LocalDateTime resolvedAt;

        public static Response from(Return r) {
            return Response.builder()
                    .id(r.getId())
                    .orderId(r.getOrder().getId())
                    .orderNumber(r.getOrder().getOrderNumber())
                    .orderItemId(r.getOrderItem().getId())
                    .productName(r.getOrderItem().getProduct().getName())
                    .reason(r.getReason())
                    .status(r.getStatus())
                    .requestedAt(r.getRequestedAt())
                    .resolvedAt(r.getResolvedAt())
                    .build();
        }
    }
}
