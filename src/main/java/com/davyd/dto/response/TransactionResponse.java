package com.davyd.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Details of a completed money transfer")
public record TransactionResponse(
        @Schema(description = "Transaction ID", example = "1")
        Long id,

        @Schema(description = "Source bank account the money was withdrawn from",
                example = "1")
        Long fromAccountId,

        @Schema(description = "Destination bank account the money was deposited to",
        example = "1")
        Long toAccountId,

        @Schema(
                description = "Amount of money that was transferred between two bank accounts",
                example = "200.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Date and time when the transaction was created",
                example = "2026-08-25T14:30:00"
        )
        LocalDateTime createdAt
) {
}
