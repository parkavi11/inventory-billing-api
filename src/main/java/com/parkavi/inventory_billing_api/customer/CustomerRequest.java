package com.parkavi.inventory_billing_api.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 200) String name,
        @Email @Size(max = 200) String email,
        @Size(max = 30) String phone) {
}