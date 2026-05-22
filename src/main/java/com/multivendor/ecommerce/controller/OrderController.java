package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.OrderDTO;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO.Response>> placeOrder(
            @Valid @RequestBody OrderDTO.PlaceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", orderService.placeOrder(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderDTO.Response>>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByCustomer(customerId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderDTO.Response>>> getByStatus(
            @PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO.Response>> updateStatus(
            @PathVariable Long id,
            @RequestBody OrderDTO.StatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(id, req.getStatus())));
    }
}
