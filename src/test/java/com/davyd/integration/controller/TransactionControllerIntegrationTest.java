package com.davyd.integration.controller;

import com.davyd.dto.request.CreateExternalTransactionRequest;
import com.davyd.dto.request.CreateTransferTransactionRequest;
import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.dto.response.UserResponse;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.repository.UserRepository;
import com.davyd.service.BankAccountService;
import com.davyd.service.TransactionService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    // =========================================================
    // GET /transactions
    // =========================================================

    @Test
    void shouldGetAllTransactions() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account.id(),
                new BigDecimal("500.00"),
                "key-1"
        );

        transactionService.withdraw(
                account.id(),
                new BigDecimal("100.00"),
                "key-2"
        );

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldReturnEmptyPageWhenNoTransactionsExist() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldGetTransactionsWithCustomPagination() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account.id(),
                new BigDecimal("100.00"),
                "key-1"
        );

        transactionService.deposit(
                account.id(),
                new BigDecimal("200.00"),
                "key-2"
        );

        transactionService.deposit(
                account.id(),
                new BigDecimal("300.00"),
                "key-3"
        );

        mockMvc.perform(get("/transactions")
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
    // GET /transactions/{id}
    // =========================================================

    @Test
    void shouldGetTransactionById() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        TransactionResponse transaction =
                transactionService.deposit(
                        account.id(),
                        new BigDecimal("500.00"),
                        "key-1"
                );

        mockMvc.perform(get(
                        "/transactions/{id}",
                        transaction.id()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaction.id()))
                .andExpect(jsonPath("$.toAccountId").value(account.id()))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void shouldReturn404WhenTransactionDoesNotExist()
            throws Exception {

        mockMvc.perform(get("/transactions/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturn400WhenTransactionIdIsNotPositive()
            throws Exception {

        mockMvc.perform(get("/transactions/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // =========================================================
    // GET /transactions/by-account/{accountId}
    // =========================================================

    @Test
    void shouldGetTransactionsByAccount() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account.id(),
                new BigDecimal("500.00"),
                "key-1"
        );

        transactionService.withdraw(
                account.id(),
                new BigDecimal("100.00"),
                "key-2"
        );

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        account.id()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldFilterTransactionsByDirection() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account1 =
                bankAccountService.createAccount(user.id());

        BankAccountResponse account2 =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account1.id(),
                new BigDecimal("500.00"),
                "deposit"
        );

        transactionService.transfer(
                account1.id(),
                account2.id(),
                new BigDecimal("100.00"),
                "transfer"
        );

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        account1.id()
                )
                        .param("direction", "FROM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fromAccountId")
                        .value(account1.id()));
    }

    @Test
    void shouldSortTransactionsByAmountDescending()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account.id(),
                new BigDecimal("100.00"),
                "key-1"
        );

        transactionService.deposit(
                account.id(),
                new BigDecimal("500.00"),
                "key-2"
        );

        transactionService.deposit(
                account.id(),
                new BigDecimal("200.00"),
                "key-3"
        );

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        account.id()
                )
                        .param("sortingMethod", "AMOUNT_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(500.00))
                .andExpect(jsonPath("$.content[1].amount").value(200.00))
                .andExpect(jsonPath("$.content[2].amount").value(100.00));
    }

    @Test
    void shouldReturn404WhenGettingTransactionsForNonExistingAccount()
            throws Exception {

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        999999L
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenAccountIdIsNotPositive()
            throws Exception {

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        0
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenDirectionIsInvalid()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        mockMvc.perform(get(
                        "/transactions/by-account/{accountId}",
                        account.id()
                )
                        .param("direction", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // POST /transactions/deposit
    // =========================================================

    @Test
    void shouldDepositMoney() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("500.00")
                );

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", "deposit-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.toAccountId").value(account.id()))
                .andExpect(jsonPath("$.amount").value(500.00));

        assertEquals(1, transactionRepository.count());

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(
                0,
                changed.balance().compareTo(
                        new BigDecimal("500.00")
                )
        );
    }

    @Test
    void shouldReturn404WhenDepositingToNonExistingAccount()
            throws Exception {

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        999999L,
                        new BigDecimal("500.00")
                );

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", "deposit-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldReturn400WhenDepositAmountIsInvalid()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("-100.00")
                );

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", "deposit-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, transactionRepository.count());
    }

    // =========================================================
    // POST /transactions/withdraw
    // =========================================================

    @Test
    void shouldWithdrawMoney() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                account.id(),
                new BigDecimal("500.00"),
                "setup-deposit"
        );

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("150.00")
                );

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Idempotency-Key", "withdraw-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fromAccountId").value(account.id()))
                .andExpect(jsonPath("$.amount").value(150.00));

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(
                0,
                changed.balance().compareTo(
                        new BigDecimal("350.00")
                )
        );

        assertEquals(2, transactionRepository.count());
    }

    @Test
    void shouldReturn409WhenWithdrawingMoreThanBalance()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("500.00")
                );

        mockMvc.perform(post("/transactions/withdraw")
                        .header("Idempotency-Key", "withdraw-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(0, transactionRepository.count());
    }

    // =========================================================
    // POST /transactions/transfer
    // =========================================================

    @Test
    void shouldTransferMoney() throws Exception {
        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse fromAccount =
                bankAccountService.createAccount(user.id());

        BankAccountResponse toAccount =
                bankAccountService.createAccount(user.id());

        transactionService.deposit(
                fromAccount.id(),
                new BigDecimal("1000.00"),
                "setup-deposit"
        );

        CreateTransferTransactionRequest request =
                new CreateTransferTransactionRequest(
                        fromAccount.id(),
                        toAccount.id(),
                        new BigDecimal("250.00")
                );

        mockMvc.perform(post("/transactions/transfer")
                        .header("Idempotency-Key", "transfer-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fromAccountId")
                        .value(fromAccount.id()))
                .andExpect(jsonPath("$.toAccountId")
                        .value(toAccount.id()))
                .andExpect(jsonPath("$.amount").value(250.00));

        BankAccountResponse changedFrom =
                bankAccountService.getAccount(fromAccount.id());

        BankAccountResponse changedTo =
                bankAccountService.getAccount(toAccount.id());

        assertEquals(
                0,
                changedFrom.balance().compareTo(
                        new BigDecimal("750.00")
                )
        );

        assertEquals(
                0,
                changedTo.balance().compareTo(
                        new BigDecimal("250.00")
                )
        );
    }

    @Test
    void shouldReturn400WhenTransferringToSameAccount()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateTransferTransactionRequest request =
                new CreateTransferTransactionRequest(
                        account.id(),
                        account.id(),
                        new BigDecimal("100.00")
                );

        mockMvc.perform(post("/transactions/transfer")
                        .header("Idempotency-Key", "transfer-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldReturn404WhenTransferAccountDoesNotExist()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse fromAccount =
                bankAccountService.createAccount(user.id());

        CreateTransferTransactionRequest request =
                new CreateTransferTransactionRequest(
                        fromAccount.id(),
                        999999L,
                        new BigDecimal("100.00")
                );

        mockMvc.perform(post("/transactions/transfer")
                        .header("Idempotency-Key", "transfer-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertEquals(0, transactionRepository.count());
    }

    // =========================================================
    // Idempotency-Key
    // =========================================================

    @Test
    void shouldReturn400WhenIdempotencyKeyIsMissing()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("100.00")
                );

        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldReturn400WhenIdempotencyKeyIsBlank()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("100.00")
                );

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", " ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldReturn400WhenIdempotencyKeyIsTooLong()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("100.00")
                );

        String tooLongKey = "a".repeat(101);

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", tooLongKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    void shouldNotCreateDuplicateTransactionForSameIdempotencyKey()
            throws Exception {

        UserResponse user =
                userService.createUser("Davyd", "davyd@gmail.com");

        BankAccountResponse account =
                bankAccountService.createAccount(user.id());

        CreateExternalTransactionRequest request =
                new CreateExternalTransactionRequest(
                        account.id(),
                        new BigDecimal("100.00")
                );

        String requestJson =
                objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", "same-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/transactions/deposit")
                        .header("Idempotency-Key", "same-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        assertEquals(1, transactionRepository.count());

        BankAccountResponse changed =
                bankAccountService.getAccount(account.id());

        assertEquals(
                0,
                changed.balance().compareTo(
                        new BigDecimal("100.00")
                )
        );
    }
}