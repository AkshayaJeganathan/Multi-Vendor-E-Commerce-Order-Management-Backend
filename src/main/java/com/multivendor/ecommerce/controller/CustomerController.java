package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.ApiResponse;
import com.multivendor.ecommerce.dto.CustomerDTO;
import com.multivendor.ecommerce.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO.Response>> register(
            @Valid @RequestBody CustomerDTO.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered", customerService.registerCustomer(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }
}
