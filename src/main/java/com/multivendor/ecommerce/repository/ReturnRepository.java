package com.multivendor.ecommerce.repository;

import com.multivendor.ecommerce.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findByOrderId(Long orderId);
    List<Return> findByStatus(Return.ReturnStatus status);
}
