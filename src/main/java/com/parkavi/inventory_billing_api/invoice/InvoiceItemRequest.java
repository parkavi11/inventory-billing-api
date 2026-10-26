package com.parkavi.inventory_billing_api.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InvoiceItemRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity) {
}