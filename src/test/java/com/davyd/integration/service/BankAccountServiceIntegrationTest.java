package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.UserDeletionNotAllowedException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.models.AccountStatus;
import com.davyd.service.BankAccountService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class BankAccountServiceIntegrationTest extends BaseServiceIntegrationTest {
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

    //getAllAccounts test
    //getAccountsByOwner test
}
