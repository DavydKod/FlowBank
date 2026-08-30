package com.davyd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Data required to create a transaction and perform a money transfer between two " +
        "bank accounts")
public record CreateTransferTransactionRequest(
        @Schema(description = "Source bank account the money will be withdrawn from",
        example = "1")
        @Positive
        long fromAccountId,

        @Schema(description = "Destination bank account the money will be deposited to",
        example = "2")
        @Positive
        long toAccountId,

        @Schema(
                description = "Amount of money to be transferred between two bank accounts",
                example = "200.00"
        )
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount
) {
}
