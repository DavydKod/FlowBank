package com.davyd.mapper;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.models.BankAccount;

public class BankAccountMapper {
    private BankAccountMapper(){}

    public static BankAccountResponse toResponse(BankAccount bankAccount){
        return new BankAccountResponse(
                bankAccount.getId(),
                bankAccount.getOwner().getId(),
                bankAccount.getBalance(),
                bankAccount.getStatus()
        );
    }
}
