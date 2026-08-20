package com.davyd.dto.response;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String message, LocalDateTime createdAt) {
}
