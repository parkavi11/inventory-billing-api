package com.parkavi.inventory_billing_api.invoice;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long productId,
        String sku,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal lineTotal) {

    public static InvoiceItemResponse from(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTaxRate(),
                item.getTaxAmount(),
                item.getLineTotal());
    }
}