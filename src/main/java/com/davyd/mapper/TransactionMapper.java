package com.davyd.mapper;

import com.davyd.dto.response.TransactionResponse;
import com.davyd.models.Transaction;

public class TransactionMapper {
    private TransactionMapper(){}

    public static TransactionResponse toResponse(Transaction transaction){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFromAccount().getId(),
                transaction.getToAccount().getId(),
                transaction.getAmount(),
                transaction.getCreatedAt()
        );
    }
}
