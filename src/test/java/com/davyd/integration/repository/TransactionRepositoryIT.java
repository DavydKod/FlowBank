package com.davyd.integration.repository;

import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.models.BankAccount;
import com.davyd.models.Transaction;
import com.davyd.models.TransactionType;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class TransactionRepositoryIT {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;


    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }


    // ---------- TEST DATA ----------

    private User createUser(String email) {
        return userRepository.save(
                new User("Davyd", email)
        );
    }

    private BankAccount createAccount(User user) {
        return bankAccountRepository.save(
                new BankAccount(user)
        );
    }


    private Transaction createTransferTransaction(
            BankAccount from,
            BankAccount to,
            String amount,
            LocalDateTime createdAt,
            String idempotencyKey
    ) {
        return transactionRepository.save(
                new Transaction(
                        TransactionType.TRANSFER,
                        from,
                        to,
                        new BigDecimal(amount),
                        createdAt,
                        idempotencyKey
                )
        );
    }


    // ============================================================
    // FIND BY ACCOUNT
    // ============================================================

    @Test
    void shouldFindTransactionsByFromAccount() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);
        BankAccount account3 = createAccount(user);

        createTransferTransaction(
                account1, account2,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account1, account3,
                "200.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account2, account1,
                "300.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        Page<Transaction> result =
                transactionRepository.findByFromAccount_Id(
                        account1.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());

        assertTrue(
                result.stream()
                        .allMatch(transaction ->
                                transaction.getFromAccount()
                                        .getId()
                                        .equals(account1.getId()))
        );
    }


    @Test
    void shouldFindTransactionsByToAccount() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);
        BankAccount account3 = createAccount(user);

        createTransferTransaction(
                account2, account1,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account3, account1,
                "200.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account1, account2,
                "300.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        Page<Transaction> result =
                transactionRepository.findByToAccount_Id(
                        account1.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());

        assertTrue(
                result.stream()
                        .allMatch(transaction ->
                                transaction.getToAccount()
                                        .getId()
                                        .equals(account1.getId()))
        );
    }


    @Test
    void shouldFindBothSentAndReceivedTransactions() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);
        BankAccount account3 = createAccount(user);

        // sent
        createTransferTransaction(
                account1, account2,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        // received
        createTransferTransaction(
                account3, account1,
                "200.00",
                LocalDateTime.now(),
                "key-2"
        );

        // unrelated
        createTransferTransaction(
                account2, account3,
                "300.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        Page<Transaction> result =
                transactionRepository
                        .findByFromAccount_IdOrToAccount_Id(
                                account1.getId(),
                                account1.getId(),
                                PageRequest.of(0, 10)
                        );

        assertEquals(2, result.getTotalElements());
    }


    // ============================================================
    // TOTALS
    // ============================================================

    @Test
    void shouldGetTotalSent() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        createTransferTransaction(
                account1, account2,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account1, account2,
                "250.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account2, account1,
                "500.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository.getTotalSent(account1.getId());

        assertEquals(
                0,
                new BigDecimal("350.00").compareTo(result)
        );
    }


    @Test
    void shouldGetTotalReceived() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        createTransferTransaction(
                account2, account1,
                "150.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account2, account1,
                "350.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account1, account2,
                "900.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository.getTotalReceived(account1.getId());

        assertEquals(
                0,
                new BigDecimal("500.00").compareTo(result)
        );
    }


    @Test
    void shouldReturnZeroWhenNoTransactionsExist() {
        User user = createUser("davyd@gmail.com");
        BankAccount account = createAccount(user);

        flushAndClearPersistenceContext();

        BigDecimal sent =
                transactionRepository.getTotalSent(account.getId());

        BigDecimal received =
                transactionRepository.getTotalReceived(account.getId());

        assertEquals(0, BigDecimal.ZERO.compareTo(sent));
        assertEquals(0, BigDecimal.ZERO.compareTo(received));
    }


    // ============================================================
    // COUNTS
    // ============================================================

    @Test
    void shouldGetTransactionCounts() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        // account1 sent x2
        createTransferTransaction(
                account1, account2,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account1, account2,
                "200.00",
                LocalDateTime.now(),
                "key-2"
        );

        // account1 received x1
        createTransferTransaction(
                account2, account1,
                "300.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        assertEquals(
                3,
                transactionRepository
                        .getTransactionCount(account1.getId())
        );

        assertEquals(
                2,
                transactionRepository
                        .getSentTransactionCount(account1.getId())
        );

        assertEquals(
                1,
                transactionRepository
                        .getReceivedTransactionCount(account1.getId())
        );
    }


    // ============================================================
    // LARGEST TRANSACTIONS
    // ============================================================

    @Test
    void shouldGetLargestSentTransaction() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        createTransferTransaction(
                account1, account2,
                "100.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account1, account2,
                "900.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account1, account2,
                "300.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository
                        .getLargestSentTransaction(account1.getId());

        assertEquals(
                0,
                new BigDecimal("900.00").compareTo(result)
        );
    }


    @Test
    void shouldGetLargestReceivedTransaction() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        createTransferTransaction(
                account2, account1,
                "150.00",
                LocalDateTime.now(),
                "key-1"
        );

        createTransferTransaction(
                account2, account1,
                "750.00",
                LocalDateTime.now(),
                "key-2"
        );

        createTransferTransaction(
                account2, account1,
                "250.00",
                LocalDateTime.now(),
                "key-3"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository
                        .getLargestReceivedTransaction(account1.getId());

        assertEquals(
                0,
                new BigDecimal("750.00").compareTo(result)
        );
    }


    // ============================================================
    // SINCE
    // ============================================================

    @Test
    void shouldGetTotalSentSinceSpecifiedTime() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        LocalDateTime since =
                LocalDateTime.of(2026, 8, 29, 12, 0);

        createTransferTransaction(
                account1, account2,
                "500.00",
                since.minusHours(1),
                "key-1"
        );

        createTransferTransaction(
                account1, account2,
                "100.00",
                since.plusHours(1),
                "key-2"
        );

        createTransferTransaction(
                account1, account2,
                "200.00",
                since.plusHours(2),
                "key-3"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository.getTotalSentSince(
                        account1.getId(),
                        since
                );

        assertEquals(
                0,
                new BigDecimal("300.00").compareTo(result)
        );
    }


    @Test
    void shouldIncludeTransactionCreatedExactlyAtSince() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        LocalDateTime since =
                LocalDateTime.of(2026, 8, 29, 12, 0);

        createTransferTransaction(
                account1,
                account2,
                "100.00",
                since,
                "key-1"
        );

        flushAndClearPersistenceContext();

        BigDecimal result =
                transactionRepository.getTotalSentSince(
                        account1.getId(),
                        since
                );

        assertEquals(
                0,
                new BigDecimal("100.00").compareTo(result)
        );
    }


    // ============================================================
    // IDEMPOTENCY
    // ============================================================

    @Test
    void shouldFindTransactionByIdempotencyKey() {
        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        Transaction transaction = createTransferTransaction(
                account1,
                account2,
                "100.00",
                LocalDateTime.now(),
                "unique-key-123"
        );

        flushAndClearPersistenceContext();

        Optional<Transaction> result =
                transactionRepository
                        .findByIdempotencyKey("unique-key-123");

        assertTrue(result.isPresent());
        assertEquals(
                transaction.getId(),
                result.get().getId()
        );
    }


    @Test
    void shouldReturnEmptyWhenIdempotencyKeyDoesNotExist() {
        Optional<Transaction> result =
                transactionRepository
                        .findByIdempotencyKey("unknown-key");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotAllowedDuplicateIdempotencyKey(){
        String key = "same-key";

        User user = createUser("davyd@gmail.com");

        BankAccount account1 = createAccount(user);
        BankAccount account2 = createAccount(user);

        createTransferTransaction(account1, account2, "200", LocalDateTime.now(), key);

        flushAndClearPersistenceContext();

        assertThrows(DataIntegrityViolationException.class, () -> {
            createTransferTransaction(account2, account1, "100", LocalDateTime.now(), key);
            entityManager.flush();
        });
    }
}
