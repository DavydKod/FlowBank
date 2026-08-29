package com.davyd.integration.service;

import com.davyd.dto.response.UserResponse;
import com.davyd.exception.UserDeletionNotAllowedException;
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
}
