package com.davyd.exception;

public class TransactionNotFoundException extends NotFoundException {

    public TransactionNotFoundException(long id) {
        super("Transaction with ID " + id + " not found");
    }
}
