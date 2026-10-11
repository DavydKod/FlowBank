package com.davyd.bankaccount.dto.response;

import com.davyd.bankaccount.AccountStatus;

import java.math.BigDecimal;

public record BankAccountResponse(
        Long id,
        Long ownerId,
        BigDecimal balance,
        BigDecimal dailyOutgoingLimit,
        AccountStatus status
) {
}
