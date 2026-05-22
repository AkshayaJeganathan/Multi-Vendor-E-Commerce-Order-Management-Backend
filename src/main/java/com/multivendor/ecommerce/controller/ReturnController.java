package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.ReturnDTO;
import com.multivendor.ecommerce.entity.Return;
import com.multivendor.ecommerce.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnDTO.Response>> requestReturn(
            @Valid @RequestBody ReturnDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return requested", returnService.requestReturn(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(returnService.getAllReturns()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(returnService.getReturnById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ReturnDTO.Response>>> getByStatus(
            @PathVariable Return.ReturnStatus status) {
        return ResponseEntity.ok(ApiResponse.success(returnService.getReturnsByStatus(status)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnDTO.Response>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Return approved", returnService.approveReturn(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReturnDTO.Response>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Return rejected", returnService.rejectReturn(id)));
    }
}
