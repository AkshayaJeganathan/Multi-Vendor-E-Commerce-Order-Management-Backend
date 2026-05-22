package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.ProductDTO;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO.Response>> addProduct(
            @Valid @RequestBody ProductDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added", productService.addProduct(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<ProductDTO.Response>>> getBySeller(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsBySeller(sellerId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO.Request req) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, req)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductDTO.Response>> updateStatus(
            @PathVariable Long id,
            @RequestParam Product.ProductStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                productService.updateProductStatus(id, status)));
    }
}
