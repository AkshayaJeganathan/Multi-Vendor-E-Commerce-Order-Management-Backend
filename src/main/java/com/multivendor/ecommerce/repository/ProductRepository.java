package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);
    List<Product> findByStatus(Product.ProductStatus status);
    List<Product> findByCategory(String category);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
}
