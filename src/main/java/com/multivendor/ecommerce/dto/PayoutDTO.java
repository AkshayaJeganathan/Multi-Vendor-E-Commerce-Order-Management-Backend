package com.multivendor.ecommerce.dto;

import com.multivendor.ecommerce.entity.Payout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayoutDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long sellerId;
        private String sellerName;
        private Long orderId;
        private String orderNumber;
        private Long orderItemId;
        private String productName;
        private BigDecimal amount;
        private BigDecimal commissionPct;
        private BigDecimal commissionAmt;
        private BigDecimal netPayout;
        private Payout.PayoutStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;

        public static Response from(Payout p) {
            return Response.builder()
                    .id(p.getId())
                    .sellerId(p.getSeller().getId())
                    .sellerName(p.getSeller().getName())
                    .orderId(p.getOrder().getId())
                    .orderNumber(p.getOrder().getOrderNumber())
                    .orderItemId(p.getOrderItem().getId())
                    .productName(p.getOrderItem().getProduct().getName())
                    .amount(p.getAmount())
                    .commissionPct(p.getCommissionPct())
                    .commissionAmt(p.getCommissionAmt())
                    .netPayout(p.getNetPayout())
                    .status(p.getStatus())
                    .createdAt(p.getCreatedAt())
                    .processedAt(p.getProcessedAt())
                    .build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SellerSummary {
        private Long sellerId;
        private String sellerName;
        private BigDecimal totalPending;
        private BigDecimal totalProcessed;
    }
}
