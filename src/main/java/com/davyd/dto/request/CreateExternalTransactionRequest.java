package com.davyd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Data required to create a transaction and perform a money transfer to withdraw or deposit")
public record CreateExternalTransactionRequest(
        @Schema(description = "Bank account the operation will be provided with",
                example = "1")
        @Positive
        long accountId,

        @Schema(
                description = "Amount of money to be transferred",
                example = "200.00"
        )
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount
) {
}
