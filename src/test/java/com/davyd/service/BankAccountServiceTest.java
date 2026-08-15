package com.davyd.service;

import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.models.AccountStatus;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BankAccountServiceTest {

    private BankAccountService bankAccountService;
    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        bankAccountRepository = mock(BankAccountRepository.class);
        userRepository = mock(UserRepository.class);

        bankAccountService = new BankAccountService(
                bankAccountRepository,
                userRepository
        );
    }

    @Test
    void shouldCreateAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result = bankAccountService.createAccount(1L);

        assertEquals(owner, result.getOwner());

        verify(userRepository).findById(1L);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void shouldThrowWhenCreatingAccountForNonExistingUser() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> bankAccountService.createAccount(1L)
        );

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));
    }

    @Test
    void shouldGetAccountById() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccount result = bankAccountService.getAccountById(1L);

        assertEquals(account, result);
    }

    @Test
    void shouldThrowWhenAccountNotFoundById() {
        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BankAccountNotFoundException.class,
                () -> bankAccountService.getAccountById(1L)
        );
    }

    @Test
    void shouldDeleteExistingAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        bankAccountService.deleteAccount(1L);

        verify(bankAccountRepository).delete(account);
    }

    @Test
    void shouldGetAccountsByOwner() {
        User owner = new User("Davyd", "davyd@gmail.com");

        BankAccount account1 = new BankAccount(owner);
        BankAccount account2 = new BankAccount(owner);

        List<BankAccount> accounts = List.of(account1, account2);

        when(userRepository.existsById(1L))
                .thenReturn(true);

        when(bankAccountRepository.findByOwner_Id(1L))
                .thenReturn(accounts);

        List<BankAccount> result =
                bankAccountService.getAccountsByOwner(1L);

        assertEquals(accounts, result);
    }

    @Test
    void shouldThrowWhenGettingAccountsForNonExistingOwner() {
        when(userRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> bankAccountService.getAccountsByOwner(1L)
        );

        verify(bankAccountRepository, never())
                .findByOwner_Id(1L);
    }

    @Test
    void shouldBlockAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccount result =
                bankAccountService.blockAccount(1L);

        assertEquals(account, result);

        assertEquals(AccountStatus.BLOCKED, result.getStatus());
    }

    @Test
    void shouldUnblockAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.blockAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccount result =
                bankAccountService.unblockAccount(1L);

        assertEquals(AccountStatus.ACTIVE, result.getStatus());
    }

    @Test
    void shouldThrowWhenUnblockingActiveAccount(){
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStatusException.class, () -> bankAccountService.unblockAccount(1L));
    }

    @Test
    void shouldCloseActiveAccount(){
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccount result =
                bankAccountService.closeAccount(1L);

        assertEquals(AccountStatus.CLOSED, result.getStatus());
    }

    @Test
    void shouldCloseBlockedAccount(){
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.blockAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccount result =
                bankAccountService.closeAccount(1L);

        assertEquals(AccountStatus.CLOSED, result.getStatus());
    }

    @Test
    void shouldThrowWhenBlockingClosedAccount(){
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.closeAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStatusException.class, () -> bankAccountService.blockAccount(1L));
    }

    @Test
    void shouldThrowWhenUnblockingClosedAccount(){
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.closeAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStatusException.class, () -> bankAccountService.unblockAccount(1L));
    }
}
