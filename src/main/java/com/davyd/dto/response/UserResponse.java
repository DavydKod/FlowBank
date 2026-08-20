package com.davyd.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email
) {
}
