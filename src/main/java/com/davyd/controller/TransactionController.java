package com.davyd.controller;

import com.davyd.dto.request.CreateExternalTransactionRequest;
import com.davyd.dto.request.CreateTransferTransactionRequest;
import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Transactions",
        description = "Money transfers and transaction history"
)
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Get all transactions",
            description = "Returns a paginated list of transactions"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable) {
        return ResponseEntity.ok(
                transactionService.getAllTransactions(pageable)
        );
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Returns a transaction associated with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction ID", example = "1")
            @PathVariable
            @Positive
            long id
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(id)
        );
    }

    @Operation(
            summary = "Get transactions by bank account",
            description = """
                    Returns a paginated list of transactions associated with
                    the specified bank account. Transactions can be filtered
                    by direction and ordered using the selected sorting method.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account transactions retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid bank account ID or query parameters"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            )
    })
    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long accountId,

            @Parameter(description = "Transaction direction: incoming or outgoing")
            @RequestParam(required = false)
            TransactionDirection direction,

            @Parameter(description = "Transaction sorting method")
            @RequestParam(required = false)
            TransactionSortingMethod sortingMethod,

            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable
            ) {

        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, direction, sortingMethod, pageable));
    }

    @Operation(
            summary = "Transfer money between bank accounts",
            description = "Transfers money from one bank account to another and" +
                    " creates a transaction. An idempotency key is required" +
                    " to prevent duplicate transfers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed and transaction created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, account ID, amount or idempotency key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Source or destination bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Transfer conflicts with the current account state or idempotency rules"
            )
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransferTransaction(
            @Parameter(
                    description = "Unique key used to prevent duplicate transfers",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateTransferTransactionRequest request
    ) {
        TransactionResponse transactionResponse = transactionService.transfer(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionResponse);
    }

    @Operation(
            summary = "Withdraw money from bank account",
            description = "Withdraw money from bank account and" +
                    " creates a transaction. An idempotency key is required" +
                    " to prevent duplicate transfers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Withdrawal completed and transaction created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, account ID, amount or idempotency key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Withdrawal conflicts with the current account state or idempotency rules"
            )
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> createWithdrawalTransaction(
            @Parameter(
                    description = "Unique key used to prevent duplicate transfers",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateExternalTransactionRequest request
    ){
        TransactionResponse transactionResponse = transactionService
                .withdraw(request.accountId(), request.amount(), idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionResponse);
    }

    @Operation(
            summary = "Deposit money to bank account",
            description = "Deposit money to bank account and" +
                    " creates a transaction. An idempotency key is required" +
                    " to prevent duplicate transfers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Deposit completed and transaction created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, account ID, amount or idempotency key"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Deposit conflicts with the current account state or idempotency rules"
            )
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> createDepositTransaction(
            @Parameter(
                    description = "Unique key used to prevent duplicate transfers",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateExternalTransactionRequest request
    ){
        TransactionResponse transactionResponse = transactionService
                .deposit(request.accountId(), request.amount(), idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionResponse);
    }
}
