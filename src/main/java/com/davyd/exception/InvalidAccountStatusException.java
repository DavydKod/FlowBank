package com.davyd.exception;

public class InvalidAccountStatusException extends RuntimeException {

    public InvalidAccountStatusException(String message) {
        super(message);
    }
}
