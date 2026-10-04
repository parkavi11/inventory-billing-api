package com.parkavi.inventory_billing_api.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate,
        @PositiveOrZero Integer reorderLevel) {
}