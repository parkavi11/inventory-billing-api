package com.parkavi.inventory_billing_api.product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        int reorderLevel,
        boolean active,
        OffsetDateTime createdAt) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getUnitPrice(),
                p.getTaxRate(),
                p.getReorderLevel(),
                p.isActive(),
                p.getCreatedAt());
    }
}