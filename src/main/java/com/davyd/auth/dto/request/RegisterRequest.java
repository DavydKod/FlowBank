package com.davyd.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Size(min = 3, max = 50) @NotBlank String name,
        @Size(min = 3, max = 50) @Email @NotBlank String email,
        @Size(min = 17, max = 128) @NotBlank String password
) {
}
