package com.davyd.service;

import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.InsufficientFundsException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.models.BankAccount;
import com.davyd.models.Transaction;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
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

    @Test
    void shouldProvideCorrectTransfer(){
        User userFrom = new User("Joel", "joel@gmail.com");
        User userTo = new User("Adriana", "adri@gmail.com");

        BankAccount accountFrom = new BankAccount(userFrom);
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(200));
        accountTo.deposit(BigDecimal.valueOf(350));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        transactionService.transfer(1L, 2L, BigDecimal.valueOf(150));

        assertEquals(BigDecimal.valueOf(50), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(500), accountTo.getBalance());

        verify(bankAccountRepository).findByIdForUpdate(1L);
        verify(bankAccountRepository).findByIdForUpdate(2L);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenFromAndToAccountSame(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 1L, BigDecimal.valueOf(150)));

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowWhenAccountBlocked(){
        User userFrom = new User("Joel", "joel@gmail.com");
        User userTo = new User("Adriana", "adri@gmail.com");

        BankAccount accountFrom = new BankAccount(userFrom);
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(200));
        accountTo.deposit(BigDecimal.valueOf(350));

        accountTo.blockAccount();

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        assertThrows(InvalidAccountStatusException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(100)));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenAccountClosed(){
        User userFrom = new User("Joel", "joel@gmail.com");
        User userTo = new User("Adriana", "adri@gmail.com");

        BankAccount accountFrom = new BankAccount(userFrom);
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(200));
        accountTo.deposit(BigDecimal.valueOf(350));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        accountFrom.closeAccount();

        assertThrows(InvalidAccountStatusException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(100)));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenNegativeAmount(){
        setUpTwoAccounts(BigDecimal.valueOf(200), BigDecimal.valueOf(350));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(-50)));
    }

    @Test
    void shouldThrowWhenZeroAmount(){
        setUpTwoAccounts(BigDecimal.valueOf(50), BigDecimal.valueOf(350));

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(0)));
    }

    @Test
    void shouldThrowWhenNotEnoughMoney(){
        setUpTwoAccounts(BigDecimal.valueOf(50), BigDecimal.valueOf(350));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(100)));
    }

    private void setUpTwoAccounts(BigDecimal fromBalance, BigDecimal toBalance){
        User userFrom = new User("Joel", "joel@gmail.com");
        User userTo = new User("Adriana", "adri@gmail.com");

        BankAccount accountFrom = new BankAccount(userFrom);
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(fromBalance);
        accountTo.deposit(toBalance);

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));
    }

    @Test
    void shouldThrowWhenFromAccountNotFound(){
        User userTo = new User("Adriana", "adri@gmail.com");

        BankAccount accountTo = new BankAccount(userTo);

        accountTo.deposit(BigDecimal.valueOf(100));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(50)));

        verify(bankAccountRepository).findByIdForUpdate(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(2L);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenToAccountNotFound(){
        User userFrom = new User("Adriana", "adri@gmail.com");

        BankAccount accountFrom = new BankAccount(userFrom);

        accountFrom.deposit(BigDecimal.valueOf(100));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.empty());

        assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.transfer(1L, 2L, BigDecimal.valueOf(50)));

        verify(bankAccountRepository).findByIdForUpdate(1L);
        verify(bankAccountRepository).findByIdForUpdate(2L);
        verify(transactionRepository, never()).save(any());
    }
}
