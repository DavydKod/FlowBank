package com.davyd.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Data required to set a new daily outgoing limit for bank account")
public record ChangeBankAccountDailyOutgoingLimitRequest (
        @Schema(
                description = "Money limit that will be set as daily outgoing limit",
                example = "5000.00"
        )
        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount
) {}