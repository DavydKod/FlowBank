package com.davyd.exception;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(){
        super("Not enough on balance to provide the operation");
    }

    public InsufficientFundsException(String message){
        super(message);
    }
}
