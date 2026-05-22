package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findBySellerId(Long sellerId);
    List<Payout> findByStatus(Payout.PayoutStatus status);
    List<Payout> findByOrderId(Long orderId);

    @Query("SELECT SUM(p.netPayout) FROM Payout p WHERE p.seller.id = :sellerId AND p.status = 'PROCESSED'")
    BigDecimal getTotalProcessedPayoutBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT SUM(p.netPayout) FROM Payout p WHERE p.seller.id = :sellerId AND p.status = 'PENDING'")
    BigDecimal getTotalPendingPayoutBySeller(@Param("sellerId") Long sellerId);
}
