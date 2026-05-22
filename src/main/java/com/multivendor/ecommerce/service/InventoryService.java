package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.InventoryDTO;
import com.multivendor.ecommerce.entity.Inventory;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.InsufficientStockException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.InventoryRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<InventoryDTO.Response> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(InventoryDTO.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryDTO.Response getInventoryByProduct(Long productId) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        return InventoryDTO.Response.from(inv);
    }

    /**
     * Admin restock - adds to total_quantity
     */
    @Transactional
    public InventoryDTO.Response addStock(Long productId, int quantity) {
        if (quantity <= 0) throw new BusinessException("Quantity to add must be positive.");
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        inv.setTotalQuantity(inv.getTotalQuantity() + quantity);
        log.info("Added {} units to productId={}", quantity, productId);
        return InventoryDTO.Response.from(inventoryRepository.save(inv));
    }

    /**
     * Called when an order is PLACED - reserves stock
     * Business Rule: inventory.reserved_quantity increases, available decreases
     */
    @Transactional
    public void reserveStock(Long productId, int quantity) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        Product product = inv.getProduct();

        if (inv.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, inv.getAvailableQuantity());
        }

        int updated = inventoryRepository.reserveStock(productId, quantity);
        if (updated == 0) {
            throw new InsufficientStockException(product.getName(), quantity, inv.getAvailableQuantity());
        }
        log.info("Reserved {} units for productId={}", quantity, productId);
    }

    /**
     * Called when order is DELIVERED - permanently deducts stock
     * Removes from both total and reserved
     */
    @Transactional
    public void confirmStockDeduction(Long productId, int quantity) {
        inventoryRepository.confirmStockReduction(productId, quantity);
        log.info("Confirmed stock deduction of {} units for productId={}", quantity, productId);
    }

    /**
     * Called when return is APPROVED - adds back to total_quantity
     * Business Rule: inventory increases on approved return
     */
    @Transactional
    public void restoreStockOnReturn(Long productId, int quantity) {
        inventoryRepository.restoreStock(productId, quantity);
        log.info("Restored {} units to productId={} due to return", quantity, productId);
    }

    /**
     * Called when order is CANCELLED - releases reservation
     */
    @Transactional
    public void releaseReservation(Long productId, int quantity) {
        inventoryRepository.releaseReservation(productId, quantity);
        log.info("Released reservation of {} units for productId={}", quantity, productId);
    }
}
