package com.parkavi.inventory_billing_api.stock;

import java.time.OffsetDateTime;

public record StockMovementResponse(
        Long id,
        Long productId,
        int quantityChange,
        StockReason reason,
        String reference,
        OffsetDateTime createdAt) {

    public static StockMovementResponse from(StockMovement m) {
        return new StockMovementResponse(
                m.getId(),
                m.getProduct().getId(),
                m.getQuantityChange(),
                m.getReason(),
                m.getReference(),
                m.getCreatedAt());
    }
}