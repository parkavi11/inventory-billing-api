package com.parkavi.inventory_billing_api.stock;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
        @NotNull Integer quantityChange,
        @NotNull StockReason reason,
        @Size(max = 100) String reference) {
}