package com.parkavi.inventory_billing_api.customer;

import java.time.OffsetDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        OffsetDateTime createdAt) {

    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getCreatedAt());
    }
}