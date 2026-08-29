package com.davyd.unit.models;

import com.davyd.exception.InsufficientFundsException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.models.AccountStatus;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        user = new User("Davyd", "davyd@gmail.com");
        account = new BankAccount(user);
    }

    @Test
    void shouldCreateBankAccountWithDefaultValues() {
        assertEquals(user, account.getOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNull(account.getId());
    }

    @Test
    void shouldDepositMoney() {
        account.deposit(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void shouldDepositMoneyMultipleTimes() {
        account.deposit(new BigDecimal("100.00"));
        account.deposit(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    @Test
    void shouldThrowWhenDepositingZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldThrowWhenDepositingNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(new BigDecimal("-10.00"))
        );
    }

    @Test
    void shouldThrowWhenDepositingNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(null)
        );
    }

    @Test
    void shouldWithdrawMoney() {
        account.deposit(new BigDecimal("200.00"));

        account.withdraw(new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    @Test
    void shouldWithdrawEntireBalance() {
        account.deposit(new BigDecimal("200.00"));

        account.withdraw(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    void shouldThrowWhenWithdrawingMoreThanBalance() {
        account.deposit(new BigDecimal("100.00"));

        assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(new BigDecimal("150.00"))
        );

        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void shouldThrowWhenWithdrawingZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldThrowWhenWithdrawingNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("-10.00"))
        );
    }

    @Test
    void shouldThrowWhenWithdrawingNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(null)
        );
    }

    @Test
    void shouldBlockActiveAccount() {
        account.blockAccount();

        assertEquals(AccountStatus.BLOCKED, account.getStatus());
    }

    @Test
    void shouldThrowWhenBlockingBlockedAccount() {
        account.blockAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                account::blockAccount
        );
    }

    @Test
    void shouldThrowWhenBlockingClosedAccount() {
        account.closeAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                account::blockAccount
        );
    }

    @Test
    void shouldUnblockBlockedAccount() {
        account.blockAccount();

        account.unblockAccount();

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void shouldThrowWhenUnblockingActiveAccount() {
        assertThrows(
                InvalidAccountStatusException.class,
                account::unblockAccount
        );
    }

    @Test
    void shouldThrowWhenUnblockingClosedAccount() {
        account.closeAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                account::unblockAccount
        );
    }

    @Test
    void shouldCloseActiveAccount() {
        account.closeAccount();

        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    void shouldCloseBlockedAccount() {
        account.blockAccount();

        account.closeAccount();

        assertEquals(AccountStatus.CLOSED, account.getStatus());
    }

    @Test
    void shouldThrowWhenClosingAlreadyClosedAccount() {
        account.closeAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                account::closeAccount
        );
    }

    @Test
    void shouldThrowWhenDepositingToBlockedAccount() {
        account.blockAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                () -> account.deposit(new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldThrowWhenWithdrawingFromBlockedAccount() {
        account.deposit(new BigDecimal("100.00"));
        account.blockAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                () -> account.withdraw(new BigDecimal("50.00"))
        );

        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void shouldThrowWhenDepositingToClosedAccount() {
        account.closeAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                () -> account.deposit(new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldThrowWhenWithdrawingFromClosedAccount() {
        account.closeAccount();

        assertThrows(
                InvalidAccountStatusException.class,
                () -> account.withdraw(new BigDecimal("50.00"))
        );
    }

    @Test
    void shouldThrowWhenClosingAccountWithBalanceNotNull(){
        account.deposit(new BigDecimal(100));

        assertThrows(InvalidAccountStatusException.class, () -> account.closeAccount());
    }

    @Test
    void shouldThrowWhenDepositingAmountWithIncorrectScale(){
        assertThrows(IllegalArgumentException.class, () ->
                account.deposit(BigDecimal.valueOf(34.675)));
    }

    @Test
    void shouldThrowWhenWithdrawingAmountWithIncorrectScale(){
        assertThrows(IllegalArgumentException.class, () ->
                account.withdraw(BigDecimal.valueOf(3.55875)));
    }

    @Test
    void shouldThrowWhenExceedingWithdrawBigDecimalLimits(){
        assertThrows(IllegalArgumentException.class, () ->
                account.withdraw(BigDecimal.valueOf(100000000000000000L)));
    }

    @Test
    void shouldThrowWhenExceedingDepositBigDecimalLimits(){
        assertThrows(IllegalArgumentException.class, () ->
                account.deposit(BigDecimal.valueOf(100000000000000000L)));
    }
}
