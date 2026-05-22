package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.SellerDTO;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<ApiResponse<SellerDTO.Response>> register(
            @Valid @RequestBody SellerDTO.Request req) {
        SellerDTO.Response resp = sellerService.registerSeller(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seller registered successfully", resp));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SellerDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getAllSellers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerDTO.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody SellerDTO.Request req) {
        return ResponseEntity.ok(ApiResponse.success("Seller updated", sellerService.updateSeller(id, req)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SellerDTO.Response>> updateStatus(
            @PathVariable Long id,
            @RequestParam Seller.SellerStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", sellerService.updateSellerStatus(id, status)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SellerDTO.Response>>> getByStatus(
            @PathVariable Seller.SellerStatus status) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellersByStatus(status)));
    }
}
