package com.davyd.transaction.exception;

public class IdempotencyKeyConflictException extends RuntimeException {
    public IdempotencyKeyConflictException(String message){
        super(message);
    }
}
