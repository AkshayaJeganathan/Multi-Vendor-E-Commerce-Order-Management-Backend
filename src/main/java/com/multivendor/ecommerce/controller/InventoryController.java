package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.InventoryDTO;
import com.multivendor.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllInventory()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryDTO.Response>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProduct(productId)));
    }

    @PostMapping("/product/{productId}/add")
    public ResponseEntity<ApiResponse<InventoryDTO.Response>> addStock(
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success("Stock added",
                inventoryService.addStock(productId, quantity)));
    }
}
