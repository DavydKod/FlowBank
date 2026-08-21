package com.davyd.exception;

public class BankAccountDailyTransferReachException extends RuntimeException {
    public BankAccountDailyTransferReachException(String message){
        super(message);
    }
}
