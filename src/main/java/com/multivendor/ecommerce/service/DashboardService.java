package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.DashboardDTO;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.Payout;
import com.multivendor.ecommerce.entity.Return;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ReturnRepository returnRepository;
    private final PayoutRepository payoutRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        long totalSellers  = sellerRepository.count();
        long activeSellers = sellerRepository.findByStatus(Seller.SellerStatus.ACTIVE).size();
        long totalProducts = productRepository.count();
        long totalCustomers = customerRepository.count();

        List<Order> allOrders = orderRepository.findAll();
        long totalOrders     = allOrders.size();
        long ordersPlaced    = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PLACED).count();
        long ordersShipped   = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED).count();
        long ordersDelivered = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        long ordersCancelled = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count();

        long pendingReturns = returnRepository.findByStatus(Return.ReturnStatus.REQUESTED).size();

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payout> pendingPayoutList = payoutRepository.findByStatus(Payout.PayoutStatus.PENDING);
        BigDecimal pendingPayouts = pendingPayoutList.stream()
                .map(Payout::getNetPayout)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDTO.builder()
                .totalSellers(totalSellers)
                .activeSellers(activeSellers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .ordersPlaced(ordersPlaced)
                .ordersShipped(ordersShipped)
                .ordersDelivered(ordersDelivered)
                .ordersCancelled(ordersCancelled)
                .pendingReturns(pendingReturns)
                .totalCustomers(totalCustomers)
                .totalRevenue(totalRevenue)
                .pendingPayouts(pendingPayouts)
                .build();
    }
}
