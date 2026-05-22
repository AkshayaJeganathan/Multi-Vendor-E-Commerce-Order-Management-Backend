package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.ProductDTO;
import com.multivendor.ecommerce.entity.Inventory;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.Seller;
import com.multivendor.ecommerce.exception.BusinessException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.InventoryRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public ProductDTO.Response addProduct(ProductDTO.Request req) {
        Seller seller = sellerRepository.findById(req.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", req.getSellerId()));

        if (seller.getStatus() != Seller.SellerStatus.ACTIVE) {
            throw new BusinessException("Cannot add product for a non-active seller.");
        }

        if (req.getSku() != null && productRepository.existsBySku(req.getSku())) {
            throw new BusinessException("SKU " + req.getSku() + " already exists.");
        }

        Product product = Product.builder()
                .seller(seller)
                .name(req.getName())
                .description(req.getDescription())
                .category(req.getCategory())
                .price(req.getPrice())
                .mrp(req.getMrp())
                .sku(req.getSku())
                .status(Product.ProductStatus.ACTIVE)
                .build();

        Product saved = productRepository.save(product);

        // Auto-create inventory record
        Inventory inventory = Inventory.builder()
                .product(saved)
                .totalQuantity(req.getInitialStock())
                .reservedQuantity(0)
                .build();
        inventoryRepository.save(inventory);

        log.info("Product created: id={}, sku={}, stock={}", saved.getId(), saved.getSku(), req.getInitialStock());

        ProductDTO.Response resp = ProductDTO.Response.from(saved);
        resp.setAvailableQuantity(req.getInitialStock());
        return resp;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> {
                    ProductDTO.Response r = ProductDTO.Response.from(p);
                    inventoryRepository.findByProductId(p.getId())
                            .ifPresent(inv -> r.setAvailableQuantity(inv.getAvailableQuantity()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO.Response getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        ProductDTO.Response r = ProductDTO.Response.from(product);
        inventoryRepository.findByProductId(id)
                .ifPresent(inv -> r.setAvailableQuantity(inv.getAvailableQuantity()));
        return r;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(p -> {
                    ProductDTO.Response r = ProductDTO.Response.from(p);
                    inventoryRepository.findByProductId(p.getId())
                            .ifPresent(inv -> r.setAvailableQuantity(inv.getAvailableQuantity()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO.Response updateProduct(Long id, ProductDTO.Request req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (req.getSku() != null && !req.getSku().equals(product.getSku())
                && productRepository.existsBySku(req.getSku())) {
            throw new BusinessException("SKU " + req.getSku() + " already exists.");
        }

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setCategory(req.getCategory());
        product.setPrice(req.getPrice());
        product.setMrp(req.getMrp());
        product.setSku(req.getSku());

        return ProductDTO.Response.from(productRepository.save(product));
    }

    @Transactional
    public ProductDTO.Response updateProductStatus(Long id, Product.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setStatus(status);
        return ProductDTO.Response.from(productRepository.save(product));
    }
}
