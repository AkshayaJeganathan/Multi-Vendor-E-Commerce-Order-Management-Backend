package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.PayoutDTO;
import com.multivendor.ecommerce.entity.Payout;
import com.multivendor.ecommerce.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayoutDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getAllPayouts()));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<PayoutDTO.Response>>> getBySeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getPayoutsBySeller(sellerId)));
    }

    @GetMapping("/seller/{sellerId}/summary")
    public ResponseEntity<ApiResponse<PayoutDTO.SellerSummary>> getSellerSummary(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getSellerPayoutSummary(sellerId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PayoutDTO.Response>>> getByStatus(
            @PathVariable Payout.PayoutStatus status) {
        return ResponseEntity.ok(ApiResponse.success(payoutService.getPayoutsByStatus(status)));
    }

    @PatchMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PayoutDTO.Response>> process(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payout processed", payoutService.processPayout(id)));
    }
}
