package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.CustomerDTO;
import com.multivendor.ecommerce.entity.Customer;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO.Response registerCustomer(CustomerDTO.Request req) {
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Customer with email " + req.getEmail() + " already exists.");
        }
        Customer customer = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .city(req.getCity())
                .pincode(req.getPincode())
                .build();
        return CustomerDTO.Response.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO.Response> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDTO.Response getCustomerById(Long id) {
        return CustomerDTO.Response.from(
                customerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }
}
