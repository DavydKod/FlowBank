package com.davyd.transaction;

import com.davyd.transaction.dto.response.TransactionResponse;

public class TransactionMapper {
    private TransactionMapper(){}

    public static TransactionResponse toResponse(Transaction transaction){
        return new TransactionResponse(
                transaction.getType(),
                transaction.getId(),
                transaction.getFromAccount() != null
                        ? transaction.getFromAccount().getId()
                        : null,
                transaction.getToAccount() != null
                        ? transaction.getToAccount().getId()
                        : null,
                transaction.getAmount(),
                transaction.getCreatedAt()
        );
    }
}
