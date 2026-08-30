package com.davyd.unit.models;

import com.davyd.models.BankAccount;
import com.davyd.models.Transaction;
import com.davyd.models.TransactionType;
import com.davyd.models.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    //----------------------------------------------------------------
    // TRANSFER TRANSACTION TESTS
    //----------------------------------------------------------------

    @Test
    void shouldCreateTransaction() {
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        BigDecimal amount = new BigDecimal("150.00");

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        Transaction transaction =
                new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, amount,
                        createdAt, "key");

        assertEquals(fromAccount, transaction.getFromAccount());
        assertEquals(toAccount, transaction.getToAccount());
        assertEquals(amount, transaction.getAmount());
        assertEquals(createdAt, transaction.getCreatedAt());

        assertNull(transaction.getId());
    }

    @Test
    void shouldThrowWhenFromAccountNull(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount toAccount = new BankAccount(user);

        BigDecimal amount = new BigDecimal("150.00");

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(TransactionType.TRANSFER,null, toAccount, amount, createdAt, "key"));
    }

    @Test
    void shouldThrowWhenToAccountNull(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user);

        BigDecimal amount = new BigDecimal("150.00");

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(TransactionType.TRANSFER, fromAccount, null, amount, createdAt, "key"));
    }

    @Test
    void shouldThrowWhenAmountNull(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, null, createdAt, "key"));
    }

    @Test
    void shouldThrowWhenAmountHasIncorrectScale(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, BigDecimal.valueOf(1.2546), createdAt, "key"));
    }

    @Test
    void shouldThrowWhenAmountNegative(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, BigDecimal.valueOf(-54), createdAt,"key"));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyNull(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, BigDecimal.valueOf(20), createdAt,null));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyBlank(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, BigDecimal.valueOf(20),createdAt, "  "));
    }

    @Test
    void shouldThrowWhenExceedingBigDecimalLimits(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(TransactionType.TRANSFER, fromAccount, toAccount, BigDecimal.valueOf(100000000000000000L), createdAt,"key"));
    }


    //----------------------------------------------------------------
    // WITHDRAWAL AND DEPOSIT TRANSACTION TESTS
    //----------------------------------------------------------------

    @Test
    void shouldAllowFromNullWhenDepositTransaction(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount toAccount = new BankAccount(user);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertDoesNotThrow(() -> new Transaction(TransactionType.DEPOSIT, null, toAccount,
                        BigDecimal.valueOf(20L), createdAt, "key"));
    }

    @Test
    void shouldThrowWhenToNullDuringDepositTransaction(){
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(TransactionType.DEPOSIT, null, null,
                        BigDecimal.valueOf(20L), createdAt, "key"));
    }

    @Test
    void shouldAllowToNullWhenWithdrawalTransaction(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertDoesNotThrow(() -> new Transaction(TransactionType.WITHDRAWAL, fromAccount, null,
                BigDecimal.valueOf(20L), createdAt, "key"));
    }

    @Test
    void shouldThrowWhenFromNullDuringWithdrawalTransaction(){
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(TransactionType.DEPOSIT, null, null,
                        BigDecimal.valueOf(20L), createdAt, "key"));
    }

    @Test
    void shouldNotAllowNotNullableFromAccountDuringDeposit(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () -> new Transaction(TransactionType.DEPOSIT, fromAccount, toAccount,
                BigDecimal.valueOf(20L), createdAt, "key"));
    }

    @Test
    void shouldNotAllowNotNullableToAccountDuringWithdrawal(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 25, 12, 0);

        assertThrows(IllegalArgumentException.class, () -> new Transaction(TransactionType.WITHDRAWAL, fromAccount, toAccount,
                BigDecimal.valueOf(20L), createdAt, "key"));
    }
}
