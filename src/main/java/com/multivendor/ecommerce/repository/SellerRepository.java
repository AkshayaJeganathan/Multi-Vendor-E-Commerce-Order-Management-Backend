package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByEmail(String email);
    List<Seller> findByStatus(Seller.SellerStatus status);
    boolean existsByEmail(String email);
}
