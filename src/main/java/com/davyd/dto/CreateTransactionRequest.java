package com.davyd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @Positive long fromAccountId,
        @Positive long toAccountId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}
