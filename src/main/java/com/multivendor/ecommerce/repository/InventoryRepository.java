package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :qty WHERE i.product.id = :productId AND (i.totalQuantity - i.reservedQuantity) >= :qty")
    int reserveStock(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :qty, i.totalQuantity = i.totalQuantity - :qty WHERE i.product.id = :productId")
    int confirmStockReduction(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.totalQuantity = i.totalQuantity + :qty WHERE i.product.id = :productId")
    int restoreStock(@Param("productId") Long productId, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :qty WHERE i.product.id = :productId")
    int releaseReservation(@Param("productId") Long productId, @Param("qty") int qty);
}
