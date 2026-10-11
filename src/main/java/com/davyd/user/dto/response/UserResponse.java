package com.davyd.user.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email
) {
}
