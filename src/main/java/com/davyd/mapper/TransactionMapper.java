package com.davyd.mapper;

import com.davyd.dto.response.TransactionResponse;
import com.davyd.models.Transaction;

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
