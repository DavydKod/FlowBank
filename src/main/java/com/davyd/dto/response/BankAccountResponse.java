package com.davyd.dto.response;

import com.davyd.models.AccountStatus;

import java.math.BigDecimal;

public record BankAccountResponse(
        Long id,
        Long ownerId,
        BigDecimal balance,
        BigDecimal dailyOutgoingLimit,
        AccountStatus status
) {
}
