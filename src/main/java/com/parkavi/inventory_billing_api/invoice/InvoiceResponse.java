package com.parkavi.inventory_billing_api.invoice;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long customerId,
        InvoiceStatus status,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal total,
        OffsetDateTime createdAt,
        OffsetDateTime confirmedAt,
        List<InvoiceItemResponse> items) {

    public static InvoiceResponse from(Invoice invoice) {
        Long customerId = invoice.getCustomer() == null ? null : invoice.getCustomer().getId();
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                customerId,
                invoice.getStatus(),
                invoice.getSubtotal(),
                invoice.getTaxTotal(),
                invoice.getTotal(),
                invoice.getCreatedAt(),
                invoice.getConfirmedAt(),
                invoice.getItems().stream().map(InvoiceItemResponse::from).toList());
    }
}