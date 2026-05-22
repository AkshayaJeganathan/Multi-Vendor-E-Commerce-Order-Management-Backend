package com.multivendor.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardDTO {
    private long totalSellers;
    private long activeSellers;
    private long totalProducts;
    private long totalOrders;
    private long ordersPlaced;
    private long ordersShipped;
    private long ordersDelivered;
    private long ordersCancelled;
    private long pendingReturns;
    private long totalCustomers;
    private BigDecimal totalRevenue;
    private BigDecimal pendingPayouts;
}
