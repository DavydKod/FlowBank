package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.exception.UserDeletionNotAllowedException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.models.AccountStatus;
import com.davyd.models.BankAccount;
import com.davyd.service.BankAccountService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class BankAccountServiceIT extends BaseServiceIT {
    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;


    @Test
    void shouldNotAllowUserDeletionWhenBankAccountExists(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        bankAccountService.createAccount(userResponse.id());

        assertThrows(UserDeletionNotAllowedException.class, () ->
                userService.deleteUser(userResponse.id()));
    }

    @Test
    void shouldCreateBankAccountForUser(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertNotNull(bankAccountResponse.id());
        assertEquals(userResponse.id(), bankAccountResponse.ownerId());
    }

    @Test
    void shouldThrowWhenCreatingBankAccountForNonExistentUser(){
        assertThrows(UserNotFoundException.class, () ->
                bankAccountService.createAccount(1L));
    }

    @Test
    void shouldGetBankAccount(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        BankAccountResponse resultBankAccount = bankAccountService.getAccount(bankAccountResponse.id());

        assertEquals(bankAccountResponse.id(), resultBankAccount.id());
        assertEquals(bankAccountResponse.ownerId(), resultBankAccount.ownerId());
    }

    @Test
    void shouldThrowWhenGettingNonExistentBankAccount(){
        assertThrows(BankAccountNotFoundException.class, () ->
                bankAccountService.getAccount(1L));
    }

    @Test
    void shouldBlockAccount(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertEquals(AccountStatus.ACTIVE, bankAccountResponse.status());

        bankAccountService.blockAccount(bankAccountResponse.id());

        assertEquals(AccountStatus.BLOCKED, bankAccountService.getAccount(bankAccountResponse.id()).status());
    }

    @Test
    void shouldUnblockAccount(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertEquals(AccountStatus.ACTIVE, bankAccountResponse.status());

        bankAccountService.blockAccount(bankAccountResponse.id());
        bankAccountService.unblockAccount(bankAccountResponse.id());

        assertEquals(AccountStatus.ACTIVE, bankAccountService.getAccount(bankAccountResponse.id()).status());
    }

    @Test
    void shouldCloseAccount(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertEquals(AccountStatus.ACTIVE, bankAccountResponse.status());

        bankAccountService.closeAccount(bankAccountResponse.id());

        assertEquals(AccountStatus.CLOSED, bankAccountService.getAccount(bankAccountResponse.id()).status());
    }

    @Test
    void shouldGetAllBankAccounts(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse2.id());
        bankAccountService.createAccount(userResponse2.id());

        Pageable pageable = PageRequest.of(0, 10);

        Page<BankAccountResponse> bankAccountsResponse = bankAccountService.getAllAccounts(pageable);

        assertEquals(5, bankAccountsResponse.getTotalElements());
        assertEquals(5, bankAccountsResponse.getContent().size());

        assertTrue(bankAccountsResponse
                .stream().anyMatch(bankAccount ->
                        Objects.equals(bankAccount.ownerId(), userResponse1.id())));

        assertTrue(bankAccountsResponse
                .stream().anyMatch(bankAccount ->
                        Objects.equals(bankAccount.ownerId(), userResponse2.id())));

        assertTrue(bankAccountsResponse.stream()
                .allMatch(bankAccount -> bankAccount.id() != null));

    }

    @Test
    void shouldGetBankAccountsWithPagination(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse2.id());
        bankAccountService.createAccount(userResponse2.id());

        Pageable pageable = PageRequest.of(0, 3, Sort.by("id").ascending());

        Page<BankAccountResponse> bankAccountsResponse = bankAccountService.getAllAccounts(pageable);

        assertEquals(5, bankAccountsResponse.getTotalElements());
        assertEquals(3, bankAccountsResponse.getContent().size());

        assertEquals(2, bankAccountsResponse.getTotalPages());
        assertEquals(0, bankAccountsResponse.getNumber());
        assertTrue(bankAccountsResponse.hasNext());
    }

    @Test
    void shouldThrowWhenGettingBankAccountsForNonExistentUser(){
        assertThrows(UserNotFoundException.class, () ->
                bankAccountService.getAccountsByOwner(1L, PageRequest.of(0, 5)));
    }

    @Test
    void shouldGetAllBankAccountsForUser(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse2.id());
        bankAccountService.createAccount(userResponse2.id());

        Pageable pageable = PageRequest.of(0, 5);

        Page<BankAccountResponse> bankAccountsResponse1 =
                bankAccountService.getAccountsByOwner(userResponse1.id(), pageable);

        assertEquals(3, bankAccountsResponse1.getTotalElements());

        assertTrue(bankAccountsResponse1.stream()
                .allMatch(bankAccount -> Objects.equals(userResponse1.id(), bankAccount.ownerId())));

        Page<BankAccountResponse> bankAccountsResponse2 =
                bankAccountService.getAccountsByOwner(userResponse2.id(), pageable);

        assertEquals(2, bankAccountsResponse2.getTotalElements());

        assertTrue(bankAccountsResponse2.stream()
                .allMatch(bankAccount -> Objects.equals(userResponse2.id(), bankAccount.ownerId())));
    }

    @Test
    void shouldGetBankAccountsForUserWithPagination(){
        String name1 = "Davyd";
        String email1 = "davyd@gmail.com";
        UserResponse userResponse1 = userService.createUser(name1, email1);

        String name2 = "John";
        String email2 = "john@gmail.com";
        UserResponse userResponse2 = userService.createUser(name2, email2);

        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse1.id());
        bankAccountService.createAccount(userResponse2.id());
        bankAccountService.createAccount(userResponse2.id());

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        Page<BankAccountResponse> bankAccountsResponse =
                bankAccountService.getAccountsByOwner(userResponse1.id(), pageable);

        assertEquals(3, bankAccountsResponse.getTotalElements());
        assertEquals(2, bankAccountsResponse.getContent().size());

        assertEquals(2, bankAccountsResponse.getTotalPages());
        assertEquals(0, bankAccountsResponse.getNumber());
        assertTrue(bankAccountsResponse.hasNext());
    }

    @Test
    void shouldThrowWhenChangingDailyOutgoingLimitForNonExistentBankAccount(){
        assertThrows(BankAccountNotFoundException.class, () ->
                bankAccountService.changeDailyOutgoingLimit(1L, new BigDecimal("1000.00")));
    }

    @Test
    void shouldChangeDailyOutgoingLimit(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        assertEquals(BankAccount.DEFAULT_DAILY_OUTGOING_LIMIT, bankAccountResponse.dailyOutgoingLimit());

        BankAccountResponse response = bankAccountService.changeDailyOutgoingLimit(bankAccountResponse.id(), new BigDecimal("2550.00"));

        assertEquals(new BigDecimal("2550.00"), response.dailyOutgoingLimit());
    }

    @Test
    void shouldThrowWhenChangingDailyOutgoingLimitForNotActiveBankAccount(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userResponse.id());

        bankAccountService.blockAccount(bankAccountResponse.id());

        assertThrows(InvalidAccountStatusException.class, () ->
                bankAccountService.changeDailyOutgoingLimit(bankAccountResponse.id(), new BigDecimal("1300.00")));

        bankAccountService.closeAccount(bankAccountResponse.id());

        assertThrows(InvalidAccountStatusException.class, () ->
                bankAccountService.changeDailyOutgoingLimit(bankAccountResponse.id(), new BigDecimal("1340.00")));

    }
}
