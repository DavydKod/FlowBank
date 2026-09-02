package com.davyd.integration.controller;

import com.davyd.dto.request.ChangeBankAccountDailyOutgoingLimitRequest;
import com.davyd.dto.request.CreateBankAccountRequest;
import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.models.AccountStatus;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import com.davyd.service.BankAccountService;
import com.davyd.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BankAccountControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @AfterEach
    void cleanUp() {
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================
    // GET /accounts
    // =========================================================

    @Test
    void shouldGetAllAccounts() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account1 =
                bankAccountService.createAccount(user.id());

        BankAccountResponse account2 =
                bankAccountService.createAccount(user.id());

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldReturnEmptyPageWhenNoAccountsExist() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldGetAccountsWithCustomPagination() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        bankAccountService.createAccount(user.id());
        bankAccountService.createAccount(user.id());
        bankAccountService.createAccount(user.id());

        mockMvc.perform(get("/accounts")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // =========================================================
    // GET /accounts/{id}
    // =========================================================

    @Test
    void shouldGetAccountById() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        mockMvc.perform(get("/accounts/{id}", account.id()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(account.id()))
                .andExpect(jsonPath("$.ownerId").value(user.id()))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.dailyOutgoingLimit").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        mockMvc.perform(get("/accounts/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturn400WhenAccountIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/accounts/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // =========================================================
    // GET /accounts/by-owner/{ownerId}
    // =========================================================

    @Test
    void shouldGetAccountsByOwner() throws Exception {
        UserResponse user1 =
                userService.createUser("Davyd", "davyd@gmail.com");

        UserResponse user2 =
                userService.createUser("Max", "max@gmail.com");

        bankAccountService.createAccount(user1.id());

        bankAccountService.createAccount(user1.id());

        bankAccountService.createAccount(user2.id());

        mockMvc.perform(get("/accounts/by-owner/{ownerId}", user1.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].ownerId").value(user1.id()))
                .andExpect(jsonPath("$.content[1].ownerId").value(user1.id()));
    }

    @Test
    void shouldGetAccountsByOwnerWithPagination() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        bankAccountService.createAccount(user.id());
        bankAccountService.createAccount(user.id());
        bankAccountService.createAccount(user.id());

        mockMvc.perform(get("/accounts/by-owner/{ownerId}", user.id())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldReturn404WhenOwnerDoesNotExist() throws Exception {
        mockMvc.perform(get("/accounts/by-owner/{ownerId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldReturn400WhenOwnerIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/accounts/by-owner/{ownerId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================
    // POST /accounts
    // =========================================================

    @Test
    void shouldCreateAccount() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        CreateBankAccountRequest request =
                new CreateBankAccountRequest(user.id());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(user.id()))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.dailyOutgoingLimit").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertEquals(1, bankAccountRepository.count());
    }

    @Test
    void shouldReturn404WhenCreatingAccountForNonExistingOwner()
            throws Exception {

        CreateBankAccountRequest request =
                new CreateBankAccountRequest(999999L);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        assertEquals(0, bankAccountRepository.count());
    }

    @Test
    void shouldReturn400WhenCreatingAccountWithInvalidOwnerId()
            throws Exception {

        CreateBankAccountRequest request =
                new CreateBankAccountRequest(0L);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        assertEquals(0, bankAccountRepository.count());
    }

    // =========================================================
    // PATCH /accounts/{id}/block
    // =========================================================

    @Test
    void shouldBlockAccount() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        mockMvc.perform(patch("/accounts/{id}/block", account.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.id()))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(AccountStatus.BLOCKED, changed.status());
    }

    @Test
    void shouldReturn404WhenBlockingNonExistingAccount() throws Exception {
        mockMvc.perform(patch("/accounts/{id}/block", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenBlockingAccountWithInvalidId()
            throws Exception {

        mockMvc.perform(patch("/accounts/{id}/block", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn409WhenBlockingAccountInInvalidState()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        bankAccountService.blockAccount(account.id());

        mockMvc.perform(patch("/accounts/{id}/block", account.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        bankAccountService.closeAccount(account.id());

        mockMvc.perform(patch("/accounts/{id}/block", account.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // =========================================================
    // PATCH /accounts/{id}/unblock
    // =========================================================

    @Test
    void shouldUnblockAccount() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        bankAccountService.blockAccount(account.id());

        mockMvc.perform(patch("/accounts/{id}/unblock", account.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.id()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(AccountStatus.ACTIVE, changed.status());
    }

    @Test
    void shouldReturn404WhenUnblockingNonExistingAccount()
            throws Exception {

        mockMvc.perform(patch("/accounts/{id}/unblock", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenUnblockingAccountWithInvalidId()
            throws Exception {

        mockMvc.perform(patch("/accounts/{id}/unblock", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn409WhenUnblockingAccountInInvalidState()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());


        mockMvc.perform(patch("/accounts/{id}/unblock", account.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        bankAccountService.closeAccount(account.id());

        mockMvc.perform(patch("/accounts/{id}/unblock", account.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // =========================================================
    // PATCH /accounts/{id}/close
    // =========================================================

    @Test
    void shouldCloseAccount() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        mockMvc.perform(patch("/accounts/{id}/close", account.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.id()))
                .andExpect(jsonPath("$.status").value("CLOSED"));

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(AccountStatus.CLOSED, changed.status());
    }

    @Test
    void shouldReturn404WhenClosingNonExistingAccount()
            throws Exception {

        mockMvc.perform(patch("/accounts/{id}/close", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenClosingAccountWithInvalidId()
            throws Exception {

        mockMvc.perform(patch("/accounts/{id}/close", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn409WhenClosingAccountInInvalidState()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        bankAccountService.closeAccount(account.id());

        mockMvc.perform(patch("/accounts/{id}/close", account.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // =========================================================
    // PATCH /accounts/{id}/dailyOutgoingLimit
    // =========================================================

    @Test
    void shouldChangeDailyOutgoingLimit() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        ChangeBankAccountDailyOutgoingLimitRequest request =
                new ChangeBankAccountDailyOutgoingLimitRequest(
                        new BigDecimal("500.00")
                );

        mockMvc.perform(patch(
                        "/accounts/{id}/dailyOutgoingLimit",
                        account.id()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.id()))
                .andExpect(jsonPath("$.dailyOutgoingLimit").value(500.00));

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(
                0,
                changed.dailyOutgoingLimit()
                        .compareTo(new BigDecimal("500.00"))
        );
    }

    @Test
    void shouldReturn404WhenChangingLimitOfNonExistingAccount()
            throws Exception {

        ChangeBankAccountDailyOutgoingLimitRequest request =
                new ChangeBankAccountDailyOutgoingLimitRequest(
                        new BigDecimal("500.00")
                );

        mockMvc.perform(patch(
                        "/accounts/{id}/dailyOutgoingLimit",
                        999999L
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenChangingLimitWithInvalidAccountId()
            throws Exception {

        ChangeBankAccountDailyOutgoingLimitRequest request =
                new ChangeBankAccountDailyOutgoingLimitRequest(
                        new BigDecimal("500.00")
                );

        mockMvc.perform(patch(
                        "/accounts/{id}/dailyOutgoingLimit",
                        0
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenDailyOutgoingLimitIsInvalid()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        ChangeBankAccountDailyOutgoingLimitRequest request =
                new ChangeBankAccountDailyOutgoingLimitRequest(
                        new BigDecimal("-100.00")
                );

        mockMvc.perform(patch(
                        "/accounts/{id}/dailyOutgoingLimit",
                        account.id()
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        BankAccountResponse unchanged =
                bankAccountService.getAccount(account.id());

        assertEquals(
                0,
                unchanged.dailyOutgoingLimit()
                        .compareTo(account.dailyOutgoingLimit())
        );
    }
}
