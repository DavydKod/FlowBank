package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.*;
import com.davyd.models.TransactionType;
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
        assertEquals(TransactionType.DEPOSIT, transactionResponse.type());
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

        TransactionResponse transactionResponse =
                transactionService.withdraw(bankAccountResponse.id(), new BigDecimal("60.00"), "key2");

        BankAccountResponse resultAccount = bankAccountService.getAccount(bankAccountResponse.id());

        assertEquals(TransactionType.WITHDRAWAL, transactionResponse.type());
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

    @Test
    void shouldTransferMoneyBetweenAccounts() {
        UserResponse user1 = userService.createUser("Davyd", "davyd1@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max1@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-1"
        );

        TransactionResponse transaction = transactionService.transfer(
                fromAccount.id(),
                toAccount.id(),
                new BigDecimal("150.00"),
                "transfer-key-1"
        );

        BankAccountResponse updatedFrom = bankAccountService.getAccount(fromAccount.id());
        BankAccountResponse updatedTo = bankAccountService.getAccount(toAccount.id());

        assertEquals(new BigDecimal("350.00"), updatedFrom.balance());
        assertEquals(new BigDecimal("150.00"), updatedTo.balance());

        assertEquals(TransactionType.TRANSFER, transaction.type());
        assertEquals(fromAccount.id(), transaction.fromAccountId());
        assertEquals(toAccount.id(), transaction.toAccountId());
        assertEquals(new BigDecimal("150.00"), transaction.amount());
    }

    @Test
    void shouldThrowWhenTransferringToSameAccount() {
        UserResponse user = userService.createUser("Davyd", "davyd2@gmail.com");
        BankAccountResponse account = bankAccountService.createAccount(user.id());

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        account.id(),
                        account.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-2"
                )
        );
    }

    @Test
    void shouldThrowWhenFromAccountDoesNotExist() {
        UserResponse user = userService.createUser("Davyd", "davyd3@gmail.com");
        BankAccountResponse toAccount = bankAccountService.createAccount(user.id());

        long nonExistingAccountId = 999999L;

        assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.transfer(
                        nonExistingAccountId,
                        toAccount.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-3"
                )
        );
    }

    @Test
    void shouldThrowWhenToAccountDoesNotExist() {
        UserResponse user = userService.createUser("Davyd", "davyd4@gmail.com");
        BankAccountResponse fromAccount = bankAccountService.createAccount(user.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-4"
        );

        long nonExistingAccountId = 999999L;

        assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        nonExistingAccountId,
                        new BigDecimal("100.00"),
                        "transfer-key-4"
                )
        );
    }

    @Test
    void shouldThrowWhenFromAccountHasInsufficientFunds() {
        UserResponse user1 = userService.createUser("Davyd", "davyd5@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max5@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("50.00"),
                "deposit-key-5"
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-5"
                )
        );
    }

    @Test
    void shouldNotChangeBalancesWhenTransferFailsDueToInsufficientFunds() {
        UserResponse user1 = userService.createUser("Davyd", "davyd6@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max6@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("50.00"),
                "deposit-key-6"
        );

        assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-6"
                )
        );

        BankAccountResponse updatedFrom = bankAccountService.getAccount(fromAccount.id());
        BankAccountResponse updatedTo = bankAccountService.getAccount(toAccount.id());

        assertEquals(new BigDecimal("50.00"), updatedFrom.balance());
        assertEquals(new BigDecimal("0.00"), updatedTo.balance());
    }

    @Test
    void shouldThrowWhenFromAccountIsBlocked() {
        UserResponse user1 = userService.createUser("Davyd", "davyd7@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max7@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-7"
        );

        bankAccountService.blockAccount(fromAccount.id());

        assertThrows(
                InvalidAccountStatusException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-7"
                )
        );
    }

    @Test
    void shouldThrowWhenToAccountIsBlocked() {
        UserResponse user1 = userService.createUser("Davyd", "davyd8@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max8@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-8"
        );

        bankAccountService.blockAccount(toAccount.id());

        assertThrows(
                InvalidAccountStatusException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("100.00"),
                        "transfer-key-8"
                )
        );
    }

    @Test
    void shouldThrowWhenDailyTransferLimitIsExceeded() {
        UserResponse user1 = userService.createUser("Davyd", "davyd9@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max9@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("5000.00"),
                "deposit-key-9"
        );

        // Тут залежить від твого API встановлення dailyTransferLimit.
        // Встановлюєш, наприклад, 1000.00.

        transactionService.transfer(
                fromAccount.id(),
                toAccount.id(),
                new BigDecimal("800.00"),
                "transfer-key-9a"
        );

        assertThrows(
                DailyTransferLimitExceededException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("300.00"),
                        "transfer-key-9b"
                )
        );
    }

    @Test
    void shouldReturnExistingTransactionForSameIdempotencyKey() {
        UserResponse user1 = userService.createUser("Davyd", "davyd10@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max10@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-10"
        );

        TransactionResponse first = transactionService.transfer(
                fromAccount.id(),
                toAccount.id(),
                new BigDecimal("100.00"),
                "transfer-key-10"
        );

        TransactionResponse second = transactionService.transfer(
                fromAccount.id(),
                toAccount.id(),
                new BigDecimal("100.00"),
                "transfer-key-10"
        );

        assertEquals(first.id(), second.id());

        BankAccountResponse updatedFrom = bankAccountService.getAccount(fromAccount.id());
        BankAccountResponse updatedTo = bankAccountService.getAccount(toAccount.id());

        assertEquals(new BigDecimal("400.00"), updatedFrom.balance());
        assertEquals(new BigDecimal("100.00"), updatedTo.balance());
    }

    @Test
    void shouldThrowWhenIdempotencyKeyIsReusedForDifferentTransfer() {
        UserResponse user1 = userService.createUser("Davyd", "davyd11@gmail.com");
        UserResponse user2 = userService.createUser("Max", "max11@gmail.com");

        BankAccountResponse fromAccount = bankAccountService.createAccount(user1.id());
        BankAccountResponse toAccount = bankAccountService.createAccount(user2.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("500.00"),
                "deposit-key-11"
        );

        transactionService.transfer(
                fromAccount.id(),
                toAccount.id(),
                new BigDecimal("100.00"),
                "same-key-11"
        );

        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> transactionService.transfer(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("200.00"),
                        "same-key-11"
                )
        );
    }

    @Test
    void shouldRejectInvalidTransferAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        1L,
                        2L,
                        new BigDecimal("-100.00"),
                        "valid-key"
                )
        );
    }

    @Test
    void shouldRejectInvalidIdempotencyKey() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.transfer(
                        1L,
                        2L,
                        new BigDecimal("100.00"),
                        "   "
                )
        );
    }


    //getTransactionsByAccount tests
}
