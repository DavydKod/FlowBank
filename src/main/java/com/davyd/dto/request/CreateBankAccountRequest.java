package com.davyd.dto.request;

import jakarta.validation.constraints.Positive;

public record CreateBankAccountRequest(
        @Positive long ownerId
) {
}
