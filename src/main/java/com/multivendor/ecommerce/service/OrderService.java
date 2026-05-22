package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.OrderDTO;
import com.multivendor.ecommerce.entity.*;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PayoutService payoutService;

    /**
     * BUSINESS RULE: On order placement → inventory reduces (reservation)
     */
    @Transactional
    public OrderDTO.Response placeOrder(OrderDTO.PlaceRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", req.getCustomerId()));

        // Validate all products first before touching inventory
        List<Product> products = new ArrayList<>();
        for (OrderDTO.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));
            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                throw new BusinessException("Product '" + product.getName() + "' is not available for purchase.");
            }
            products.add(product);
        }

        // Reserve stock for all items (throws InsufficientStockException if any fail)
        for (int i = 0; i < req.getItems().size(); i++) {
            inventoryService.reserveStock(products.get(i).getId(), req.getItems().get(i).getQuantity());
        }

        // Build order
        String orderNumber = generateOrderNumber();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .status(Order.OrderStatus.PLACED)
                .shippingAddress(req.getShippingAddress() != null ?
                        req.getShippingAddress() : customer.getAddress())
                .build();

        // Build order items and compute total
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < req.getItems().size(); i++) {
            Product p = products.get(i);
            int qty = req.getItems().get(i).getQuantity();
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
            items.add(OrderItem.builder()
                    .order(order)
                    .product(p)
                    .seller(p.getSeller())
                    .quantity(qty)
                    .unitPrice(p.getPrice())
                    .totalPrice(lineTotal)
                    .build());
        }
        order.setTotalAmount(total);
        order.setOrderItems(items);

        Order saved = orderRepository.save(order);
        log.info("Order placed: orderNumber={}, customerId={}, total={}", orderNumber, customer.getId(), total);
        return OrderDTO.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO.Response> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO.Response getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return OrderDTO.Response.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO.Response> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId)
                .stream().map(OrderDTO.Response::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO.Response> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream().map(OrderDTO.Response::from).collect(Collectors.toList());
    }

    /**
     * BUSINESS RULE: Strict order status transition pipeline:
     *   PLACED → CONFIRMED → SHIPPED → DELIVERED
     *   PLACED/CONFIRMED → CANCELLED  (releases inventory reservation)
     *   DELIVERED → RETURN_REQUESTED (via return module)
     *
     *   On DELIVERED → payout is calculated for seller
     *   On CANCELLED → inventory reservation released
     */
    @Transactional
    public OrderDTO.Response updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateStatusTransition(order.getStatus(), newStatus);

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            // BUSINESS RULE: Trigger payout calculation on delivery
            payoutService.createPayoutsForOrder(order);
            // Permanently deduct stock
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.confirmStockDeduction(item.getProduct().getId(), item.getQuantity());
            }
            log.info("Order DELIVERED: payouts generated for orderNumber={}", order.getOrderNumber());
        } else if (newStatus == Order.OrderStatus.CANCELLED) {
            // BUSINESS RULE: Release inventory reservation on cancel
            if (oldStatus == Order.OrderStatus.PLACED || oldStatus == Order.OrderStatus.CONFIRMED) {
                for (OrderItem item : order.getOrderItems()) {
                    inventoryService.releaseReservation(item.getProduct().getId(), item.getQuantity());
                }
                log.info("Order CANCELLED: inventory released for orderNumber={}", order.getOrderNumber());
            }
        }

        Order saved = orderRepository.save(order);
        return OrderDTO.Response.from(saved);
    }

    // -------------------------------------------------------
    // Strict pipeline validation
    // -------------------------------------------------------
    private void validateStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        boolean valid = switch (current) {
            case PLACED    -> next == Order.OrderStatus.CONFIRMED || next == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> next == Order.OrderStatus.SHIPPED   || next == Order.OrderStatus.CANCELLED;
            case SHIPPED   -> next == Order.OrderStatus.DELIVERED;
            case DELIVERED -> next == Order.OrderStatus.RETURN_REQUESTED;
            case RETURN_REQUESTED -> next == Order.OrderStatus.RETURNED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("Invalid status transition: " + current + " → " + next);
        }
    }

    private String generateOrderNumber() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = new Random().nextInt(9000) + 1000;
        return "ORD-" + ts + "-" + rand;
    }
}
