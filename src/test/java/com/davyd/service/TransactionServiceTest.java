package com.davyd.service;

import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.models.Transaction;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private TransactionService transactionService;
    private TransactionRepository transactionRepository;
    private BankAccountRepository bankAccountRepository;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        bankAccountRepository = mock(BankAccountRepository.class);

        transactionService = new TransactionService(
                transactionRepository,
                bankAccountRepository
        );
    }

    @Test
    void shouldGetTransactionById() {
        Transaction transaction = mock(Transaction.class);

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        Transaction result =
                transactionService.getTransactionById(1L);

        assertEquals(transaction, result);
    }

    @Test
    void shouldThrowWhenTransactionNotFoundById() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(1L)
        );
    }

    @Test
    void shouldGetAllTransactions() {
        Transaction transaction1 = mock(Transaction.class);
        Transaction transaction2 = mock(Transaction.class);

        List<Transaction> transactions =
                List.of(transaction1, transaction2);

        when(transactionRepository.findAll())
                .thenReturn(transactions);

        List<Transaction> result =
                transactionService.getAllTransactions();

        assertEquals(transactions, result);
    }

    @Test
    void shouldGetFromTransactionsByAccount() {
        long accountId = 1L;

        Transaction transaction = mock(Transaction.class);
        List<Transaction> transactions = List.of(transaction);

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        when(bankAccountRepository.existsById(accountId))
                .thenReturn(true);

        when(transactionRepository.findByFromAccount_Id(accountId, sort))
                .thenReturn(transactions);

        List<Transaction> result =
                transactionService.getTransactionsByAccount(
                        accountId,
                        TransactionDirection.FROM,
                        TransactionSortingMethod.CREATED_AT_DESC
                );

        assertEquals(transactions, result);

        verify(transactionRepository)
                .findByFromAccount_Id(accountId, sort);
    }

    @Test
    void shouldGetToTransactionsByAccount() {
        long accountId = 1L;

        Transaction transaction = mock(Transaction.class);
        List<Transaction> transactions = List.of(transaction);

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "amount"
        );

        when(bankAccountRepository.existsById(accountId))
                .thenReturn(true);

        when(transactionRepository.findByToAccount_Id(accountId, sort))
                .thenReturn(transactions);

        List<Transaction> result =
                transactionService.getTransactionsByAccount(
                        accountId,
                        TransactionDirection.TO,
                        TransactionSortingMethod.AMOUNT_ASC
                );

        assertEquals(transactions, result);

        verify(transactionRepository)
                .findByToAccount_Id(accountId, sort);
    }

    @Test
    void shouldGetAllTransactionsByAccountWhenDirectionIsNull() {
        long accountId = 1L;

        Transaction transaction = mock(Transaction.class);
        List<Transaction> transactions = List.of(transaction);

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        when(bankAccountRepository.existsById(accountId))
                .thenReturn(true);

        when(transactionRepository.findByFromAccount_IdOrToAccount_Id(
                accountId,
                accountId,
                sort
        )).thenReturn(transactions);

        List<Transaction> result =
                transactionService.getTransactionsByAccount(
                        accountId,
                        null,
                        null
                );

        assertEquals(transactions, result);

        verify(transactionRepository)
                .findByFromAccount_IdOrToAccount_Id(
                        accountId,
                        accountId,
                        sort
                );
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        long accountId = 1L;

        when(bankAccountRepository.existsById(accountId))
                .thenReturn(false);

        assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.getTransactionsByAccount(
                        accountId,
                        TransactionDirection.FROM,
                        TransactionSortingMethod.CREATED_AT_DESC
                )
        );

        verify(transactionRepository, never())
                .findByFromAccount_Id(anyLong(), any(Sort.class));

        verify(transactionRepository, never())
                .findByToAccount_Id(anyLong(), any(Sort.class));

        verify(transactionRepository, never())
                .findByFromAccount_IdOrToAccount_Id(
                        anyLong(),
                        anyLong(),
                        any(Sort.class)
                );
    }

    @Test
    void shouldUseDefaultSortingWhenSortingMethodIsNull() {
        long accountId = 1L;

        Sort expectedSort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        when(bankAccountRepository.existsById(accountId))
                .thenReturn(true);

        when(transactionRepository.findByFromAccount_Id(
                accountId,
                expectedSort
        )).thenReturn(List.of());

        transactionService.getTransactionsByAccount(
                accountId,
                TransactionDirection.FROM,
                null
        );

        verify(transactionRepository)
                .findByFromAccount_Id(
                        accountId,
                        expectedSort
                );
    }
}
