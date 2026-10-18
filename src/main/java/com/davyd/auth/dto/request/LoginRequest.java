package com.davyd.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Size(min = 3, max = 50) @NotBlank @Email String email,
        @NotBlank String password) {
}
