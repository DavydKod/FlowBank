package com.davyd.service;

import com.davyd.dto.response.BankAccountResponse;
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

class BankAccountServiceTest {

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
        User owner = mock(User.class);

        when(owner.getId()).thenReturn(1L);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(owner));

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankAccountResponse result =
                bankAccountService.createAccount(1L);

        assertEquals(1L, result.ownerId());
        assertEquals(AccountStatus.ACTIVE, result.status());

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

        verify(userRepository).findById(1L);

        verify(bankAccountRepository, never())
                .save(any(BankAccount.class));
    }

    @Test
    void shouldGetAccountById() {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);

        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccountResponse result =
                bankAccountService.getAccount(1L);

        assertEquals(account.getId(), result.id());
        assertEquals(owner.getId(), result.ownerId());
        assertEquals(account.getBalance(), result.balance());
        assertEquals(account.getStatus(), result.status());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenAccountNotFoundById() {
        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BankAccountNotFoundException.class,
                () -> bankAccountService.getAccount(1L)
        );

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldDeleteExistingAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        bankAccountService.deleteAccount(1L);

        verify(bankAccountRepository).findById(1L);
        verify(bankAccountRepository).delete(account);
    }

    @Test
    void shouldGetAccountsByOwner() {
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(1L);

        BankAccount account1 = new BankAccount(owner);
        BankAccount account2 = new BankAccount(owner);

        List<BankAccount> accounts = List.of(account1, account2);

        when(userRepository.existsById(1L))
                .thenReturn(true);

        when(bankAccountRepository.findByOwner_Id(1L))
                .thenReturn(accounts);

        List<BankAccountResponse> result =
                bankAccountService.getAccountsByOwner(1L);

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).ownerId());
        assertEquals(account1.getBalance(), result.get(0).balance());
        assertEquals(account1.getStatus(), result.get(0).status());

        assertEquals(1L, result.get(1).ownerId());
        assertEquals(account2.getBalance(), result.get(1).balance());
        assertEquals(account2.getStatus(), result.get(1).status());

        verify(userRepository).existsById(1L);
        verify(bankAccountRepository).findByOwner_Id(1L);
    }

    @Test
    void shouldThrowWhenGettingAccountsForNonExistingOwner() {
        when(userRepository.existsById(1L))
                .thenReturn(false);

        assertThrows(
                UserNotFoundException.class,
                () -> bankAccountService.getAccountsByOwner(1L)
        );

        verify(userRepository).existsById(1L);

        verify(bankAccountRepository, never())
                .findByOwner_Id(anyLong());
    }

    @Test
    void shouldBlockAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccountResponse result =
                bankAccountService.blockAccount(1L);

        assertEquals(AccountStatus.BLOCKED, result.status());
        assertEquals(AccountStatus.BLOCKED, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldUnblockAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.blockAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccountResponse result =
                bankAccountService.unblockAccount(1L);

        assertEquals(AccountStatus.ACTIVE, result.status());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenUnblockingActiveAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidAccountStatusException.class,
                () -> bankAccountService.unblockAccount(1L)
        );

        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldCloseActiveAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccountResponse result =
                bankAccountService.closeAccount(1L);

        assertEquals(AccountStatus.CLOSED, result.status());
        assertEquals(AccountStatus.CLOSED, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldCloseBlockedAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.blockAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        BankAccountResponse result =
                bankAccountService.closeAccount(1L);

        assertEquals(AccountStatus.CLOSED, result.status());
        assertEquals(AccountStatus.CLOSED, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenBlockingClosedAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.closeAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidAccountStatusException.class,
                () -> bankAccountService.blockAccount(1L)
        );

        assertEquals(AccountStatus.CLOSED, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenUnblockingClosedAccount() {
        User owner = new User("Davyd", "davyd@gmail.com");
        BankAccount account = new BankAccount(owner);

        account.closeAccount();

        when(bankAccountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidAccountStatusException.class,
                () -> bankAccountService.unblockAccount(1L)
        );

        assertEquals(AccountStatus.CLOSED, account.getStatus());

        verify(bankAccountRepository).findById(1L);
    }
}