package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.service.BankAccountService;
import com.davyd.service.TransactionService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceIntegrationTest extends BaseServiceIntegrationTest{
    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;

    /*
    @Test
    void shouldGetTransaction(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse1 = bankAccountService.createAccount(userResponse.id());
        BankAccountResponse bankAccountResponse2 = bankAccountService.createAccount(userResponse.id());

        TransactionResponse responseTransaction = transactionService.transfer(
                bankAccountResponse1.id(),
                bankAccountResponse2.id(),
                new BigDecimal("100.00"),
                "key"
        );

        TransactionResponse resultTransaction = transactionService.getTransactionById(responseTransaction.id());


    }
    SHOULD HAVE THE ABILITY TO DEPOSIT TO BANK ACCOUNT FROM TERMINAL*/

    @Test
    void shouldThrowWhenGettingNonExistentTransaction(){
        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactionById(1L));
    }

    //transfer tests
    //getTransactionsByAccount tests
    //getAllTransactions tests
}
