package com.davyd.bankaccount;

import com.davyd.bankaccount.dto.response.BankAccountResponse;

public class BankAccountMapper {
    private BankAccountMapper(){}

    public static BankAccountResponse toResponse(BankAccount bankAccount){
        return new BankAccountResponse(
                bankAccount.getId(),
                bankAccount.getOwner().getId(),
                bankAccount.getBalance(),
                bankAccount.getDailyOutgoingLimit(),
                bankAccount.getStatus()
        );
    }
}
