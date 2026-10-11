package com.davyd.bankaccount.dto.request;

import jakarta.validation.constraints.Positive;

public record CreateBankAccountRequest(
        @Positive long ownerId
) {
}
