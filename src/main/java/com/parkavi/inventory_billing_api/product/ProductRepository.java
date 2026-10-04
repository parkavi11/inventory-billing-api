package com.parkavi.inventory_billing_api.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    List<Product> findAllByOrderByNameAsc();

    List<Product> findByActiveTrueOrderByNameAsc();
}