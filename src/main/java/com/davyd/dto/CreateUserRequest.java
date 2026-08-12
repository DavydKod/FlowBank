package com.davyd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Size(min = 3, max = 50) @NotBlank String name,
        @Size(min = 3, max = 50) @Email @NotBlank String email) {
}
