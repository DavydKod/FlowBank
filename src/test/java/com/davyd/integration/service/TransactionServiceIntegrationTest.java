package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.IdempotencyKeyConflictException;
import com.davyd.exception.InsufficientFundsException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.service.BankAccountService;
import com.davyd.service.TransactionService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionServiceIntegrationTest extends BaseServiceIntegrationTest{
    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;


    @Test
    void shouldDepositToAccount(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        TransactionResponse transactionResponse =
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("100.00"), "key");

        assertNotNull(transactionResponse.id());
        assertNull(transactionResponse.fromAccountId());
        assertEquals(bankAccountResponse.ownerId(), transactionResponse.toAccountId());
        assertEquals(new BigDecimal("100.00"), transactionResponse.amount());

        BankAccountResponse resultAccount = bankAccountService.getAccount(bankAccountResponse.id());

        assertEquals(transactionResponse.amount(), resultAccount.balance());
    }

    @Test
    void shouldWithdrawFromAccount(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        transactionService.deposit(bankAccountResponse.id(), new BigDecimal("100.00"), "key");

        transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("60.00"), "key2");

        BankAccountResponse resultAccount = bankAccountService.getAccount(bankAccountResponse.id());

        assertEquals(new BigDecimal("40.00"), resultAccount.balance());
    }

    @Test
    void shouldNotWithdrawWhenNotEnoughMoney(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("100.00"), "key"));
    }

    @Test
    void shouldThrowWhenWithdrawalAccountNotExist(){
        assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.withdraw(1L, new BigDecimal("45.50") , "key"));
    }

    @Test
    void shouldThrowWhenDepositAccountNotExist(){
        assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.deposit(1L, new BigDecimal("45.50") , "key"));
    }

    @Test
    void shouldThrowWhenIncorrectAmountOfWithdrawal(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), null , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("0.00") , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("-5.00") , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("15.000") , "key"));
    }

    @Test
    void shouldThrowWhenIncorrectAmountOfDeposit(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), null , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("0.00") , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("-5.00") , "key"));
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("15.000") , "key"));
    }

    @Test
    void shouldThrowWhenIncorrectIdempotencyKeyDuringWithdrawal(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal(100L), null));
    }

    @Test
    void shouldThrowWhenIncorrectIdempotencyKeyDuringDeposit(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal(100L), null));
    }

    @Test
    void shouldGetTransaction(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse1 = bankAccountService.createAccount(userResponse.id());

        TransactionResponse responseTransaction = transactionService.deposit(
                bankAccountResponse1.id(),
                new BigDecimal("100.00"),
                "key"
        );

        TransactionResponse resultTransaction = transactionService.getTransactionById(responseTransaction.id());

        assertNotNull(resultTransaction.id());
        assertEquals(responseTransaction.amount(), resultTransaction.amount());
    }

    @Test
    void shouldThrowWhenGettingNonExistentTransaction(){
        assertThrows(TransactionNotFoundException.class, () ->
                transactionService.getTransactionById(1L));
    }

    @Test
    void shouldGetAllTransactions(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        BankAccountResponse account1 = bankAccountService.createAccount(userResponse1.id());
        BankAccountResponse account2 = bankAccountService.createAccount(userResponse2.id());

        TransactionResponse deposit1 = transactionService.deposit(account1.id(), new BigDecimal("250.00"), "key1");
        TransactionResponse deposit2 = transactionService.deposit(account2.id(), new BigDecimal("300.00"), "key2");
        TransactionResponse withdraw1 = transactionService.withdraw(account1.id(), new BigDecimal("60.00"), "key3");
        TransactionResponse withdraw2 = transactionService.withdraw(account2.id(), new BigDecimal("130.00"), "key4");

        Pageable pageable = PageRequest.of(0, 5);

        Page<TransactionResponse> transactions = transactionService.getAllTransactions(pageable);

        assertEquals(4, transactions.getTotalElements());
        assertEquals(4, transactions.getContent().size());

        Set<Long> expectedIds = Set.of(
                deposit1.id(),
                deposit2.id(),
                withdraw1.id(),
                withdraw2.id()
        );

        Set<Long> actualIds = transactions.stream()
                .map(TransactionResponse::id)
                .collect(Collectors.toSet());

        assertEquals(expectedIds, actualIds);
    }

    @Test
    void shouldGetTransactionsWithPageable(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        BankAccountResponse account1 = bankAccountService.createAccount(userResponse1.id());
        BankAccountResponse account2 = bankAccountService.createAccount(userResponse2.id());

        transactionService.deposit(account1.id(), new BigDecimal("250.00"), "key1");
        transactionService.deposit(account2.id(), new BigDecimal("300.00"), "key2");
        transactionService.withdraw(account1.id(), new BigDecimal("60.00"), "key3");
        transactionService.withdraw(account2.id(), new BigDecimal("130.00"), "key4");

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        Page<TransactionResponse> transactions = transactionService.getAllTransactions(pageable);

        assertEquals(4, transactions.getTotalElements());
        assertEquals(2, transactions.getContent().size());

        assertEquals(2, transactions.getTotalPages());
        assertEquals(0, transactions.getNumber());
        assertTrue(transactions.hasNext());
    }

    @Test
    void shouldThrowWhenNotUniqueIdempotencyKeyDuringDifferentWithdrawal(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        transactionService.deposit(bankAccountResponse.id(), new BigDecimal("100.00"), "key");

        assertThrows(IdempotencyKeyConflictException.class, () ->
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("50.00"), "key"));
    }

    @Test
    void shouldThrowWhenNotUniqueIdempotencyKeyDuringDifferentDeposit(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        transactionService.deposit(bankAccountResponse.id(), new BigDecimal("100.00"), "key");

        assertThrows(IdempotencyKeyConflictException.class, () ->
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("50.00"), "key"));
    }

    @Test
    void shouldReturnWhenNotUniqueIdempotencyKeyDuringSameWithdrawal(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        transactionService.deposit(bankAccountResponse.id(), new BigDecimal("100.00"), "key");

        TransactionResponse transactionResponse =
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("60"), "same-key");

        TransactionResponse sameTransactionResponse =
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("60"), "same-key");

        assertEquals(transactionResponse.id(), sameTransactionResponse.id());
    }

    @Test
    void shouldReturnWhenNotUniqueIdempotencyKeyDuringSameDeposit(){
        UserResponse userResponse = userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        TransactionResponse transactionResponse =
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("60"), "same-key");

        TransactionResponse sameTransactionResponse =
                transactionService.deposit(bankAccountResponse.id(), new BigDecimal("60"), "same-key");

        assertEquals(transactionResponse.id(), sameTransactionResponse.id());
    }


    //transfer tests
    //getTransactionsByAccount tests
}
