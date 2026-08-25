package com.davyd.controller;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.request.CreateBankAccountRequest;
import com.davyd.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@Tag(
        name = "Bank Accounts",
        description = "Bank account management"
)
@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Operation(
            summary = "Get all bank accounts",
            description = "Returns a paginated list of all bank accounts"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank accounts retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<Page<BankAccountResponse>> getAllAccounts(
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable) {
        return ResponseEntity.ok(bankAccountService.getAllAccounts(pageable));
    }

    @Operation(
            summary = "Get bank account by ID",
            description = "Returns the bank account associated with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid bank account ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id
    ) {
        return ResponseEntity.ok(bankAccountService.getAccount(id));
    }

    @Operation(
            summary = "Get bank accounts by owner",
            description = """
                    Returns a paginated list of bank accounts belonging
                    to the specified user.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User bank accounts retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid owner ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<Page<BankAccountResponse>> getAccountsByOwner(
            @Parameter(description = "User ID", example = "1")
            @PathVariable
            @Positive
            long ownerId,

            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                bankAccountService.getAccountsByOwner(ownerId, pageable)
        );
    }

    @Operation(
            summary = "Create bank account",
            description = "Creates a new bank account for the specified user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank account created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or owner ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account owner not found"
            )
    })
    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @RequestBody
            @Valid
            CreateBankAccountRequest request
    ) {
        BankAccountResponse accountResponse = bankAccountService.createAccount(request.ownerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountResponse);
    }

    @Operation(
            summary = "Block bank account",
            description = "Changes the specified bank account status to BLOCKED"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account blocked successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid bank account ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bank account cannot be blocked in its current state"
            )
    })
    @PatchMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> blockAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.blockAccount(id);

        return ResponseEntity.ok(accountResponse);
    }

    @Operation(
            summary = "Unblock bank account",
            description = "Changes the specified bank account status to ACTIVE"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account unblocked successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid bank account ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bank account cannot be unblocked in its current state"
            )
    })
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<BankAccountResponse> unblockAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.unblockAccount(id);

        return ResponseEntity.ok(accountResponse);
    }

    @Operation(
            summary = "Close bank account",
            description = """
                    Changes the specified bank account status to CLOSED.
                    A closed account cannot be reactivated.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank account closed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid bank account ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bank account not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bank account cannot be closed in its current state"
            )
    })
    @PatchMapping("/{id}/close")
    public ResponseEntity<BankAccountResponse> closeAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.closeAccount(id);

        return ResponseEntity.ok(accountResponse);
    }
}
