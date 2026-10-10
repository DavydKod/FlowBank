package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.IdempotencyKeyConflictException;
import com.davyd.models.Transaction;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.service.BankAccountService;
import com.davyd.service.TransactionService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

public class TransactionServiceConcurrencyIT extends BaseServiceIT {
    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;

    @MockitoSpyBean
    private BankAccountRepository bankAccountRepository;

    @MockitoSpyBean
    private TransactionRepository transactionRepository;

    @Test
    void shouldThrowWhenIncorrectIdempotencyKeyForConcurrentDeposit() throws Exception {
        UserResponse user = userService.createUser("Davyd", "davyd@gmail.com");
        BankAccountResponse account1 = bankAccountService.createAccount(user.id());
        BankAccountResponse account2 = bankAccountService.createAccount(user.id());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        doAnswer(invocation -> Optional.empty())
                .when(transactionRepository)
                .findByIdempotencyKey(anyString());

        // It will throw even with identical transactions (different idempotency keys) (instead of
        // returning a transaction) because it's an exception at the lowest layer
        try {
            Future<TransactionResponse> future1 = executor.submit(() -> {
                return transactionService.deposit(account1.id(), BigDecimal.valueOf(300).setScale(2), "duplicate-key");
            });

            Future<TransactionResponse> future2 = executor.submit(() -> {
                return transactionService.deposit(account2.id(), BigDecimal.valueOf(300).setScale(2), "duplicate-key");

            });

            int successful = 0;
            int failed = 0;

            for (Future<?> future: List.of(future1, future2)){
                try{
                    future.get();
                    successful++;
                } catch (ExecutionException e){
                    assertInstanceOf(
                            IdempotencyKeyConflictException.class,
                            e.getCause()
                    );
                    failed++;
                }
            }

            assertEquals(1, successful);
            assertEquals(1, failed);

            verify(transactionRepository, times(4))
                    .findByIdempotencyKey(anyString());

            verify(bankAccountRepository, times(2))
                    .findByIdForUpdate(anyLong());

            verify(transactionRepository, times(2))
                    .saveAndFlush(any(Transaction.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldThrowWhenIncorrectIdempotencyKeyForConcurrentWithdrawal() throws Exception {
        UserResponse user = userService.createUser("Davyd", "davyd@gmail.com");
        BankAccountResponse account = bankAccountService.createAccount(user.id());

        transactionService.deposit(account.id(), new BigDecimal("450.00"), "key1");

        clearInvocations(transactionRepository, bankAccountRepository);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        doAnswer(invocation -> Optional.empty())
                .when(transactionRepository)
                .findByIdempotencyKey(anyString());

        // It will throw even with identical transactions (different idempotency keys) (instead of
        // returning a transaction) because it's an exception at the lowest layer
        try {
            Future<TransactionResponse> future1 = executor.submit(() ->
                transactionService.withdraw(account.id(), new BigDecimal("200.00"), "duplicate-key")
            );

            Future<TransactionResponse> future2 = executor.submit(() ->
                    transactionService.withdraw(account.id(), new BigDecimal("200.00"), "duplicate-key")
            );

            int successful = 0;
            int failed = 0;

            for (Future<?> future: List.of(future1, future2)){
                try{
                    future.get();
                    successful++;
                } catch (ExecutionException e){
                    assertInstanceOf(
                            IdempotencyKeyConflictException.class,
                            e.getCause()
                    );
                    failed++;
                }
            }

            assertEquals(1, successful);
            assertEquals(1, failed);

            verify(transactionRepository, times(4))
                    .findByIdempotencyKey(anyString());

            verify(bankAccountRepository, times(2))
                    .findByIdForUpdate(anyLong());

            verify(transactionRepository, times(2))
                    .saveAndFlush(any(Transaction.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldThrowWhenIncorrectIdempotencyKeyForConcurrentTransfer() throws Exception {
        UserResponse user1 = userService.createUser("Davyd", "davyd@gmail.com");
        UserResponse user2 = userService.createUser("Alex", "alex@gmail.com");

        BankAccountResponse account1 = bankAccountService.createAccount(user1.id());
        BankAccountResponse account2 = bankAccountService.createAccount(user2.id());

        transactionService.deposit(account1.id(), new BigDecimal("500.00"), "key-1");
        transactionService.deposit(account2.id(), new BigDecimal("500.00"), "key-2");

        clearInvocations(transactionRepository, bankAccountRepository);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        doAnswer(invocation -> Optional.empty())
                .when(transactionRepository)
                .findByIdempotencyKey(anyString());

        // It will throw even with identical transactions (different idempotency keys) (instead of
        // returning a transaction) because it's an exception at the lowest layer
        try {
            Future<TransactionResponse> future1 = executor.submit(() ->
                    transactionService.transfer(account1.id(), account2.id(), new BigDecimal("200.00"), "duplicate-key")
            );

            Future<TransactionResponse> future2 = executor.submit(() ->
                    transactionService.transfer(account1.id(), account2.id(), new BigDecimal("200.00"), "duplicate-key")
            );

            int successful = 0;
            int failed = 0;

            for (Future<?> future: List.of(future1, future2)){
                try{
                    future.get();
                    successful++;
                } catch (ExecutionException e){
                    assertInstanceOf(
                            IdempotencyKeyConflictException.class,
                            e.getCause()
                    );
                    failed++;
                }
            }

            assertEquals(1, successful);
            assertEquals(1, failed);

            verify(transactionRepository, times(4))
                    .findByIdempotencyKey(anyString());

            verify(bankAccountRepository, times(4))
                    .findByIdForUpdate(anyLong());

            verify(transactionRepository, times(2))
                    .saveAndFlush(any(Transaction.class));
        } finally {
            executor.shutdownNow();
        }
    }
}
