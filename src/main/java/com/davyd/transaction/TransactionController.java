package com.davyd.transaction;

import com.davyd.security.principal.FlowBankPrincipal;
import com.davyd.transaction.dto.request.CreateExternalTransactionRequest;
import com.davyd.transaction.dto.request.CreateTransferTransactionRequest;
import com.davyd.transaction.dto.response.TransactionResponse;
import com.davyd.transaction.dto.TransactionDirection;
import com.davyd.transaction.dto.TransactionSortingMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Transactions",
        description = "Transactions and history using session authentication"
)
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Returns a transaction if the authenticated user owns its source or destination account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user owns neither account involved in the transaction",
                    content = @Content
            ),
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
            long id,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
            ) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(principal.getUserId(), id)
        );
    }

    @Operation(
            summary = "Get transactions by bank account",
            description = """
                    Returns transactions for a bank account owned by the authenticated user.
                    FROM selects outgoing transactions; TO selects incoming transactions.
                    Omitting direction returns both. Sorting defaults to CREATED_AT_DESC.
                    sortingMethod overrides the pageable sort parameter.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user",
                    content = @Content
            ),
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

            @Parameter(description = "FROM for outgoing, TO for incoming; omit for both")
            @RequestParam(required = false)
            TransactionDirection direction,

            @Parameter(description = "Defaults to CREATED_AT_DESC; overrides pageable sort")
            @RequestParam(required = false)
            TransactionSortingMethod sortingMethod,

            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
            ) {

        return ResponseEntity.ok(transactionService.getTransactionsByAccount(principal.getUserId(), accountId, direction, sortingMethod, pageable));
    }

    @Operation(
            summary = "Transfer money between bank accounts",
            description = "Transfers money from an account owned by the authenticated user to another account. "
                    + "Accounts must be distinct and ACTIVE. Requires sufficient funds and compliance with "
                    + "the source account's rolling 24-hour outgoing limit. Requires a valid CSRF token. "
                    + "Repeating an identical request with the same Idempotency-Key returns the existing "
                    + "transaction; reusing a key with different transaction details returns 409."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Source account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed, or existing transaction returned for an identical retry"
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
                    description = "Inactive account, insufficient funds, outgoing limit exceeded, idempotency conflict or concurrent operation conflict"
            )
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransferTransaction(
            @Parameter(
                    description = "Key shared across transaction types; reuse only for an identical request",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateTransferTransactionRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        TransactionResponse transactionResponse = transactionService.transfer(
                principal.getUserId(),
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
            description = "Simulates an external withdrawal from an ACTIVE account owned by the authenticated user. "
                    + "Requires sufficient funds and compliance with the rolling 24-hour outgoing limit. "
                    + "Requires a valid CSRF token and an Idempotency-Key. An identical retry with the same "
                    + "key returns the existing transaction; different transaction details return 409."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Withdrawal completed, or existing transaction returned for an identical retry"
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
                    description = "Inactive account, insufficient funds, outgoing limit exceeded, idempotency conflict or concurrent operation conflict"
            )
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> createWithdrawalTransaction(
            @Parameter(
                    description = "Key shared across transaction types; reuse only for an identical request",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateExternalTransactionRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ){
        TransactionResponse transactionResponse = transactionService
                .withdraw(principal.getUserId(), request.accountId(), request.amount(), idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionResponse);
    }

    @Operation(
            summary = "Deposit money to bank account",
            description = "Simulates an external deposit into an ACTIVE account owned by the authenticated user "
                    + "for this educational project. No external payment confirmation is performed. "
                    + "Requires a valid CSRF token and an Idempotency-Key. An identical retry with the same "
                    + "key returns the existing transaction; different transaction details return 409."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Deposit completed, or existing transaction returned for an identical retry"
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
                    description = "Inactive account, idempotency conflict or concurrent operation conflict"
            )
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> createDepositTransaction(
            @Parameter(
                    description = "Key shared across transaction types; reuse only for an identical request",
                    example = "a7196b87-2f45-46d3-af42-4d8f8ff90fe2", required = true)
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,

            @RequestBody
            @Valid
            CreateExternalTransactionRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ){
        TransactionResponse transactionResponse = transactionService
                .deposit(principal.getUserId(), request.accountId(), request.amount(), idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionResponse);
    }
}
