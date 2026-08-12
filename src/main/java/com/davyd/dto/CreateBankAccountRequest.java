package com.davyd.dto;

import jakarta.validation.constraints.Positive;

public record CreateBankAccountRequest(
        @Positive long ownerId
) {
}
