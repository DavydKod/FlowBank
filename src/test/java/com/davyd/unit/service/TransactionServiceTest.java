package com.davyd.unit.service;

import com.davyd.bankaccount.exception.BankAccountNotFoundException;
import com.davyd.bankaccount.exception.DailyTransferLimitExceededException;
import com.davyd.bankaccount.exception.InvalidAccountStatusException;
import com.davyd.transaction.dto.TransactionDirection;
import com.davyd.transaction.dto.TransactionSortingMethod;
import com.davyd.transaction.dto.response.TransactionResponse;
import com.davyd.transaction.TransactionMapper;
import com.davyd.bankaccount.BankAccount;
import com.davyd.transaction.Transaction;
import com.davyd.transaction.TransactionType;
import com.davyd.transaction.exception.IdempotencyKeyConflictException;
import com.davyd.transaction.exception.InsufficientFundsException;
import com.davyd.transaction.exception.TransactionNotFoundException;
import com.davyd.user.User;
import com.davyd.bankaccount.BankAccountRepository;
import com.davyd.transaction.TransactionRepository;
import com.davyd.transaction.TransactionService;
import com.davyd.bankaccount.OutgoingTransactionsLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.davyd.security.authorization.OwnershipValidator;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private OutgoingTransactionsLimitService transferLimitService;

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-08-25T12:00:00Z"),
            ZoneOffset.UTC);

    @Spy
    private OwnershipValidator ownershipValidator = new OwnershipValidator();

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldGetTransactionById() {
        Transaction transaction = createTransaction();

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        TransactionResponse expected =
                TransactionMapper.toResponse(transaction);

        TransactionResponse result =
                transactionService.getTransactionById(1L,1L);

        assertEquals(expected, result);

        verify(transactionRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenTransactionNotFoundById() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(1L,1L)
        );

        verify(transactionRepository).findById(1L);
    }

    @Test
    void shouldGetFromTransactionsByAccount() {
        long accountId = 1L;

        Transaction transaction = createTransaction();

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        Pageable pageable = PageRequest.of(0, 20, sort);

        Page<Transaction> transactions = new PageImpl<>(
                List.of(transaction),
                pageable,
                1

        );

        when(bankAccountRepository.findOwnerIdByAccountId(accountId))
                .thenReturn(Optional.of(transaction.getFromAccount().getOwner().getId()));

        when(transactionRepository.findByFromAccount_Id(accountId, pageable))
                .thenReturn(transactions);

        Page<TransactionResponse> expected = transactions
                .map(TransactionMapper::toResponse);

        Page<TransactionResponse> result =
                transactionService.getTransactionsByAccount(1L,
                        accountId,
                        TransactionDirection.FROM,
                        TransactionSortingMethod.CREATED_AT_DESC,
                        pageable
                );

        assertEquals(expected, result);

        verify(bankAccountRepository).findOwnerIdByAccountId(accountId);

        verify(transactionRepository)
                .findByFromAccount_Id(accountId, pageable);
    }

    @Test
    void shouldGetToTransactionsByAccount() {
        long accountId = 1L;

        Transaction transaction = createTransaction();

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "amount"
        );

        Pageable pageable = PageRequest.of(0, 20, sort);

        Page<Transaction> transactions = new PageImpl<>(
                List.of(transaction),
                pageable,
                1

        );

        when(bankAccountRepository.findOwnerIdByAccountId(accountId))
                .thenReturn(Optional.of(transaction.getFromAccount().getOwner().getId()));

        when(transactionRepository.findByToAccount_Id(accountId, pageable))
                .thenReturn(transactions);

        Page<TransactionResponse> expected = transactions
                .map(TransactionMapper::toResponse);

        Page<TransactionResponse> result =
                transactionService.getTransactionsByAccount(1L,
                        accountId,
                        TransactionDirection.TO,
                        TransactionSortingMethod.AMOUNT_ASC,
                        pageable
                );

        assertEquals(expected, result);

        verify(bankAccountRepository).findOwnerIdByAccountId(accountId);

        verify(transactionRepository)
                .findByToAccount_Id(accountId, pageable);
    }

    @Test
    void shouldGetAllTransactionsByAccountWhenDirectionIsNull() {
        long accountId = 1L;

        Transaction transaction = createTransaction();

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        Pageable pageable = PageRequest.of(0, 20, sort);

        Page<Transaction> transactions = new PageImpl<>(
                List.of(transaction),
                pageable,
                1

        );

        when(bankAccountRepository.findOwnerIdByAccountId(accountId))
                .thenReturn(Optional.of(transaction.getFromAccount().getOwner().getId()));

        when(transactionRepository.findByFromAccount_IdOrToAccount_Id(
                accountId,
                accountId,
                pageable
        )).thenReturn(transactions);

        Page<TransactionResponse> expected = transactions
                .map(TransactionMapper::toResponse);

        Page<TransactionResponse> result =
                transactionService.getTransactionsByAccount(1L,
                        accountId,
                        null,
                        null,
                        pageable
                );

        assertEquals(expected, result);

        verify(bankAccountRepository).findOwnerIdByAccountId(accountId);

        verify(transactionRepository)
                .findByFromAccount_IdOrToAccount_Id(
                        accountId,
                        accountId,
                        pageable
                );
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        long accountId = 1L;

        Pageable pageable = PageRequest.of(0, 20);

        when(bankAccountRepository.findOwnerIdByAccountId(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.getTransactionsByAccount(1L,
                        accountId,
                        TransactionDirection.FROM,
                        TransactionSortingMethod.CREATED_AT_DESC,
                        pageable
                )
        );

        verify(transactionRepository, never())
                .findByFromAccount_Id(anyLong(), any(Pageable.class));

        verify(transactionRepository, never())
                .findByToAccount_Id(anyLong(), any(Pageable.class));

        verify(transactionRepository, never())
                .findByFromAccount_IdOrToAccount_Id(
                        anyLong(),
                        anyLong(),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldUseDefaultSortingWhenSortingMethodIsNull() {
        long accountId = 1L;
        Transaction transaction = createTransaction();

        Sort expectedSort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        Pageable inputPageable = PageRequest.of(0, 20);
        Pageable expectedPageable = PageRequest.of(0, 20, expectedSort);

        when(bankAccountRepository.findOwnerIdByAccountId(accountId))
                .thenReturn(Optional.of(transaction.getFromAccount().getOwner().getId()));

        when(transactionRepository.findByFromAccount_Id(
                accountId,
                expectedPageable
        )).thenReturn(Page.empty(expectedPageable));

        transactionService.getTransactionsByAccount(1L,
                accountId,
                TransactionDirection.FROM,
                null,
                inputPageable
        );

        verify(transactionRepository)
                .findByFromAccount_Id(
                        accountId,
                        expectedPageable
                );
    }

    @Test
    void shouldProvideCorrectTransfer(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(200));
        accountTo.deposit(BigDecimal.valueOf(350));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        when(transactionRepository.saveAndFlush(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response =
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(150), "key");

        assertEquals(BigDecimal.valueOf(50), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(500), accountTo.getBalance());

        assertEquals(LocalDateTime.now(clock), response.createdAt());

        verify(bankAccountRepository).findByIdForUpdate(1L);
        verify(bankAccountRepository).findByIdForUpdate(2L);

        verify(transactionRepository).saveAndFlush(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenFromAndToAccountSame(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 1L, 1L, BigDecimal.valueOf(150), "key"));

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowWhenAccountBlocked(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(200));
        accountTo.deposit(BigDecimal.valueOf(350));

        accountTo.blockAccount();

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        assertThrows(InvalidAccountStatusException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(100), "key"));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenAccountClosed(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        accountTo.deposit(BigDecimal.valueOf(350));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        accountFrom.closeAccount();

        assertThrows(InvalidAccountStatusException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(100), "key"));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenNegativeAmount(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(-50), "key"));

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowWhenZeroAmount(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(0), "key"));

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowWhenNotEnoughMoney(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(50));
        accountTo.deposit(BigDecimal.valueOf(350));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(100), "key"));
    }

    @Test
    void shouldThrowWhenFromAccountNotFound(){
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountTo = new BankAccount(userTo);

        accountTo.deposit(BigDecimal.valueOf(100));

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.empty());

        BankAccountNotFoundException exception = assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(50), "key"));

        assertEquals("Bank account with ID 1 not found", exception.getMessage());

        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(anyLong());
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenToAccountNotFound(){
        User userFrom = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));

        accountFrom.deposit(BigDecimal.valueOf(100));

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));
        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.empty());

        BankAccountNotFoundException exception = assertThrows(BankAccountNotFoundException.class, () ->
                transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(50), "key"));

        assertEquals("Bank account with ID 2 not found", exception.getMessage());

        verify(bankAccountRepository).findByIdForUpdate(1L);
        verify(bankAccountRepository).findByIdForUpdate(2L);
        verify(transactionRepository, never()).saveAndFlush(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenTransferAmountHasIncorrectScale(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 1L, 2L,
                        BigDecimal.valueOf(50.089), "key"));

        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository);
    }

    private Transaction createTransaction() {
        User sender = new User(
                "Davyd",
                "davyd@gmail.com"
        );
        ReflectionTestUtils.setField(sender, "id", 1L);

        User receiver = new User(
                "Receiver",
                "receiver@gmail.com"
        );
        ReflectionTestUtils.setField(receiver, "id", 2L);

        BankAccount fromAccount = new BankAccount(sender);
        BankAccount toAccount = new BankAccount(receiver);

        return new Transaction(
                TransactionType.TRANSFER,
                fromAccount,
                toAccount,
                new BigDecimal("100.00"),
                LocalDateTime.now(clock),
                "key"
        );
    }

    @Test
    void shouldRejectTransferWhenDailyLimitWouldBeExceeded() {
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        accountFrom.deposit(BigDecimal.valueOf(900));
        accountTo.deposit(BigDecimal.valueOf(350));

        BigDecimal transferAmount = BigDecimal.valueOf(150);

        when(bankAccountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(accountFrom));

        when(bankAccountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(accountTo));

        doThrow(new DailyTransferLimitExceededException(
                "24-hour transfer limit exceeded"
        ))
                .when(transferLimitService)
                .validateDailyTransferLimit(
                        accountFrom,
                        transferAmount
                );

        assertThrows(
                DailyTransferLimitExceededException.class,
                () -> transactionService.transfer(1L,
                        1L,
                        2L,
                        transferAmount,
                        "key"
                )
        );

        assertEquals(
                0,
                BigDecimal.valueOf(900)
                        .compareTo(accountFrom.getBalance())
        );

        assertEquals(
                0,
                BigDecimal.valueOf(350)
                        .compareTo(accountTo.getBalance())
        );

        verify(transferLimitService)
                .validateDailyTransferLimit(
                        accountFrom,
                        transferAmount
                );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyNull(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 2L, 1L, BigDecimal.valueOf(100), null));

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transferLimitService);
    }

    @Test
    void shouldThrowWhenIdempotencyKeyBlank(){
        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 2L, 1L,
                        BigDecimal.valueOf(100), "    "));

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transferLimitService);
    }

    @Test
    void shouldThrowWhenIdenticalIdempotencyKeysButDifferentAmounts(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        ReflectionTestUtils.setField(accountFrom, "id", 1L);
        ReflectionTestUtils.setField(accountTo, "id", 2L);

        accountFrom.deposit(BigDecimal.valueOf(900));
        accountTo.deposit(BigDecimal.valueOf(350));

        BigDecimal transferAmount = BigDecimal.valueOf(150);

        Transaction transaction =
                new Transaction(TransactionType.TRANSFER, accountFrom, accountTo,
                        BigDecimal.valueOf(50), LocalDateTime.now(clock), "key");

        when(transactionRepository.findByIdempotencyKey("key"))
                .thenReturn(Optional.of(transaction));

        assertThrows(IdempotencyKeyConflictException.class, () ->
                transactionService.transfer(1L, 1L, 2L, transferAmount, "key"));

        verifyNoInteractions(transferLimitService);
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenIdenticalIdempotencyKeysButDifferentFromIds(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        User anotherUserFrom = new User("Davyd", "dav@gmail.com");
        ReflectionTestUtils.setField(anotherUserFrom, "id", 1L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        BankAccount anotherAccountFrom = new BankAccount(anotherUserFrom);

        ReflectionTestUtils.setField(accountFrom, "id", 1L);
        ReflectionTestUtils.setField(accountTo, "id", 2L);
        ReflectionTestUtils.setField(anotherAccountFrom, "id", 3L);

        accountFrom.deposit(BigDecimal.valueOf(900));
        accountTo.deposit(BigDecimal.valueOf(350));

        BigDecimal transferAmount = BigDecimal.valueOf(150);

        Transaction transaction =
                new Transaction(TransactionType.TRANSFER, anotherAccountFrom, accountTo,
                        BigDecimal.valueOf(50), LocalDateTime.now(clock), "key");

        when(transactionRepository.findByIdempotencyKey("key"))
                .thenReturn(Optional.of(transaction));

        assertThrows(IdempotencyKeyConflictException.class, () ->
                transactionService.transfer(1L, 1L, 2L, transferAmount, "key"));

        verifyNoInteractions(transferLimitService);
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenIdenticalIdempotencyKeysButDifferentToIds(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        User anotherUserTo = new User("Davyd", "dav@gmail.com");
        ReflectionTestUtils.setField(anotherUserTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        BankAccount anotherAccountTo = new BankAccount(anotherUserTo);

        ReflectionTestUtils.setField(accountFrom, "id", 1L);
        ReflectionTestUtils.setField(accountTo, "id", 2L);
        ReflectionTestUtils.setField(anotherAccountTo, "id", 3L);

        accountFrom.deposit(BigDecimal.valueOf(900));
        accountTo.deposit(BigDecimal.valueOf(350));

        BigDecimal transferAmount = BigDecimal.valueOf(150);

        Transaction transaction =
                new Transaction(TransactionType.TRANSFER, accountFrom, anotherAccountTo,
                        BigDecimal.valueOf(50), LocalDateTime.now(clock), "key");

        when(transactionRepository.findByIdempotencyKey("key"))
                .thenReturn(Optional.of(transaction));

        assertThrows(IdempotencyKeyConflictException.class, () ->
                transactionService.transfer(1L, 1L, 2L, transferAmount, "key"));

        verifyNoInteractions(transferLimitService);
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldReturnPreviousTransactionWhenIdempotencyKeysAndValuesIdentical(){
        User userFrom = new User("Joel", "joel@gmail.com");
        ReflectionTestUtils.setField(userFrom, "id", 1L);
        User userTo = new User("Adriana", "adri@gmail.com");
        ReflectionTestUtils.setField(userTo, "id", 2L);

        BankAccount accountFrom = new BankAccount(userFrom);

        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(userFrom.getId()));
        BankAccount accountTo = new BankAccount(userTo);

        ReflectionTestUtils.setField(accountFrom, "id", 1L);
        ReflectionTestUtils.setField(accountTo, "id", 2L);

        accountFrom.deposit(BigDecimal.valueOf(900));
        accountTo.deposit(BigDecimal.valueOf(350));

        BigDecimal transferAmount = BigDecimal.valueOf(150);

        Transaction transaction =
                new Transaction(TransactionType.TRANSFER, accountFrom, accountTo,
                        BigDecimal.valueOf(150), LocalDateTime.now(clock), "key");

        ReflectionTestUtils.setField(transaction, "id", 1L);

        when(transactionRepository.findByIdempotencyKey("key"))
                .thenReturn(Optional.of(transaction));

        TransactionResponse transactionResponse =
                transactionService.transfer(1L, 1L, 2L, transferAmount, "key");

        assertEquals(1L, transactionResponse.id());
        assertEquals(1L, transactionResponse.fromAccountId());
        assertEquals(2L, transactionResponse.toAccountId());
        assertEquals(BigDecimal.valueOf(150), transactionResponse.amount());

        verifyNoInteractions(transferLimitService);
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verify(bankAccountRepository, never()).findByIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyExceedsMaxCharacters(){
        StringBuilder keyBuilder = new StringBuilder("");

        keyBuilder.append("keys".repeat(26));

        String exceededKey = keyBuilder.toString();

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transfer(1L, 2L, 1L,
                        BigDecimal.valueOf(100), exceededKey));

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(bankAccountRepository);
        verifyNoInteractions(transferLimitService);
    }

    @Test
    void shouldThrowWhenExceedingBigDecimalLimitsInTransfer(){
        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(1L, 1L, 2L, BigDecimal.valueOf(100000000000000000L), "key"));
    }

    @Test
    void shouldRejectGetTransactionsByAccountWhenCurrentUserIsNotOwner() {
        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(1L));

        BankAccountNotFoundException exception = assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.getTransactionsByAccount(2L, 1L, null, null, PageRequest.of(0, 20))
        );

        assertEquals(new BankAccountNotFoundException(1L).getMessage(), exception.getMessage());
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository, transferLimitService);
    }

    @Test
    void shouldRejectTransferWhenCurrentUserIsNotOwner() {
        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(1L));

        BankAccountNotFoundException exception = assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.transfer(2L, 1L, 3L, BigDecimal.TEN, "key")
        );

        assertEquals(new BankAccountNotFoundException(1L).getMessage(), exception.getMessage());
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository, transferLimitService);
    }

    @Test
    void shouldRejectWithdrawWhenCurrentUserIsNotOwner() {
        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(1L));

        BankAccountNotFoundException exception = assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.withdraw(2L, 1L, BigDecimal.TEN, "key")
        );

        assertEquals(new BankAccountNotFoundException(1L).getMessage(), exception.getMessage());
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository, transferLimitService);
    }

    @Test
    void shouldRejectDepositWhenCurrentUserIsNotOwner() {
        when(bankAccountRepository.findOwnerIdByAccountId(1L))
                .thenReturn(Optional.of(1L));

        BankAccountNotFoundException exception = assertThrows(
                BankAccountNotFoundException.class,
                () -> transactionService.deposit(2L, 1L, BigDecimal.TEN, "key")
        );

        assertEquals(new BankAccountNotFoundException(1L).getMessage(), exception.getMessage());
        verify(bankAccountRepository).findOwnerIdByAccountId(1L);
        verifyNoMoreInteractions(bankAccountRepository);
        verifyNoInteractions(transactionRepository, transferLimitService);
    }

    @Test
    void shouldRejectGetTransactionByIdWhenCurrentUserOwnsNeitherAccount() {
        Transaction transaction = createTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(999L, 1L)
        );

        assertEquals(new TransactionNotFoundException(1L).getMessage(), exception.getMessage());
        verify(transactionRepository).findById(1L);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoInteractions(bankAccountRepository, transferLimitService);
    }
}
