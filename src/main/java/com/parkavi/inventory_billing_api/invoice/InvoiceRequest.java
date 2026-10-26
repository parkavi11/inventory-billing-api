package com.parkavi.inventory_billing_api.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvoiceRequest(
        Long customerId,
        @NotEmpty @Valid List<InvoiceItemRequest> items) {
}