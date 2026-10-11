package com.davyd.bankaccount.exception;

import com.davyd.common.exception.NotFoundException;

public class BankAccountNotFoundException extends NotFoundException {

    public BankAccountNotFoundException(long id) {
        super("Bank account with ID " + id + " not found");
    }
}
