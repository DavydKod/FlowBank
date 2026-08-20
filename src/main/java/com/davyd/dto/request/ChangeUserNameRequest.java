package com.davyd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserNameRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String name
) {
}