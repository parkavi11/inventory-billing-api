package com.parkavi.inventory_billing_api.stock;

public record StockLevelResponse(
        Long productId,
        String sku,
        long currentStock,
        int reorderLevel,
        boolean lowStock) {
}