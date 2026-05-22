package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.SellerDTO;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final SellerRepository sellerRepository;

    @Transactional
    public SellerDTO.Response registerSeller(SellerDTO.Request req) {
        if (sellerRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Seller with email " + req.getEmail() + " already exists.");
        }
        Seller seller = Seller.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .gstNumber(req.getGstNumber())
                .address(req.getAddress())
                .status(Seller.SellerStatus.ACTIVE)
                .build();
        Seller saved = sellerRepository.save(seller);
        log.info("Registered new seller: id={}, name={}", saved.getId(), saved.getName());
        return SellerDTO.Response.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SellerDTO.Response> getAllSellers() {
        return sellerRepository.findAll()
                .stream()
                .map(SellerDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SellerDTO.Response getSellerById(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        return SellerDTO.Response.from(seller);
    }

    @Transactional
    public SellerDTO.Response updateSeller(Long id, SellerDTO.Request req) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));

        // If email changed, check uniqueness
        if (!seller.getEmail().equals(req.getEmail()) &&
                sellerRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email " + req.getEmail() + " is already taken.");
        }

        seller.setName(req.getName());
        seller.setEmail(req.getEmail());
        seller.setPhone(req.getPhone());
        seller.setGstNumber(req.getGstNumber());
        seller.setAddress(req.getAddress());

        return SellerDTO.Response.from(sellerRepository.save(seller));
    }

    @Transactional
    public SellerDTO.Response updateSellerStatus(Long id, Seller.SellerStatus status) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        seller.setStatus(status);
        return SellerDTO.Response.from(sellerRepository.save(seller));
    }

    @Transactional(readOnly = true)
    public List<SellerDTO.Response> getSellersByStatus(Seller.SellerStatus status) {
        return sellerRepository.findByStatus(status)
                .stream()
                .map(SellerDTO.Response::from)
                .collect(Collectors.toList());
    }
}
