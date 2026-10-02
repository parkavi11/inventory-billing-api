package com.parkavi.inventory_billing_api.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("select coalesce(sum(m.quantityChange), 0L) from StockMovement m where m.product.id = :productId")
    long currentStock(@Param("productId") Long productId);

    List<StockMovement> findByProductIdOrderByIdDesc(Long productId);
}