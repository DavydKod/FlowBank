package com.davyd.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void shouldCreateTransaction() {
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        BigDecimal amount = new BigDecimal("150.00");

        LocalDateTime beforeCreation = LocalDateTime.now();

        Transaction transaction =
                new Transaction(fromAccount, toAccount, amount, "key");

        LocalDateTime afterCreation = LocalDateTime.now();

        assertEquals(fromAccount, transaction.getFromAccount());
        assertEquals(toAccount, transaction.getToAccount());
        assertEquals(amount, transaction.getAmount());

        assertNotNull(transaction.getCreatedAt());
        assertFalse(transaction.getCreatedAt().isBefore(beforeCreation));
        assertFalse(transaction.getCreatedAt().isAfter(afterCreation));

        assertNull(transaction.getId());
    }

    @Test
    void shouldThrowWhenFromAccountNull(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount toAccount = new BankAccount(user);

        BigDecimal amount = new BigDecimal("150.00");

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(null, toAccount, amount, "key"));
    }

    @Test
    void shouldThrowWhenToAccountNull(){
        User user = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user);

        BigDecimal amount = new BigDecimal("150.00");

        assertThrows(IllegalArgumentException.class, () ->
                new Transaction(fromAccount, null, amount, "key"));
    }

    @Test
    void shouldThrowWhenAmountNull(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        assertThrows(IllegalArgumentException.class, () -> new Transaction(fromAccount, toAccount, null, "key"));
    }

    @Test
    void shouldThrowWhenAmountHasIncorrectScale(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(fromAccount, toAccount, BigDecimal.valueOf(1.2546), "key"));
    }

    @Test
    void shouldThrowWhenAmountNegative(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(fromAccount, toAccount, BigDecimal.valueOf(-54), "key"));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyNull(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(fromAccount, toAccount, BigDecimal.valueOf(20), null));
    }

    @Test
    void shouldThrowWhenIdempotencyKeyBlank(){
        User user1 = new User("Davyd", "davyd@gmail.com");
        User user2 = new User("Alex", "alex@gmail.com");

        BankAccount fromAccount = new BankAccount(user1);
        BankAccount toAccount = new BankAccount(user2);

        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(fromAccount, toAccount, BigDecimal.valueOf(20), "  "));
    }
}
