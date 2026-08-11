package org.example.exception;

public class BankAccountNotFoundException extends NotFoundException {

    public BankAccountNotFoundException(long id) {
        super("Bank account with ID " + id + " not found");
    }
}
