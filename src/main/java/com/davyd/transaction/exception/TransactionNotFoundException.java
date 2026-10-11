package com.davyd.transaction.exception;

import com.davyd.common.exception.NotFoundException;

public class TransactionNotFoundException extends NotFoundException {

    public TransactionNotFoundException(long id) {
        super("Transaction with ID " + id + " not found");
    }
}
