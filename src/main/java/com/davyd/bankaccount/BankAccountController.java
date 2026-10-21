package com.davyd.bankaccount;

import com.davyd.bankaccount.dto.request.ChangeBankAccountDailyOutgoingLimitRequest;
import com.davyd.bankaccount.dto.response.BankAccountResponse;
import com.davyd.security.principal.FlowBankPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


@Tag(
        name = "Bank Accounts",
        description = "Current user bank account management using session authentication"
)
@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Operation(
            summary = "Get bank account by ID",
            description = "Returns the specified bank account if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
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
            long id,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
            ) {
        return ResponseEntity.ok(bankAccountService.getAccount(principal.getUserId(), id));
    }

    @Operation(
            summary = "Get the current user's bank accounts",
            description = """
                    Returns a paginated list of bank accounts belonging to the user
                    authenticated by the session cookie.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "User bank accounts retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user no longer exists"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<Page<BankAccountResponse>> getAccountsByOwner(
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
            ) {
        return ResponseEntity.ok(
                bankAccountService.getAccountsByOwner(principal.getUserId(), pageable)
        );
    }

    @Operation(
            summary = "Create bank account",
            description = "Creates a new bank account for the user authenticated by the session cookie. "
                    + "No request body is required. Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank account created successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user no longer exists"
            )
    })
    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        BankAccountResponse accountResponse = bankAccountService.createAccount(principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountResponse);
    }

    @Operation(
            summary = "Block bank account",
            description = "Changes a bank account owned by the authenticated user to BLOCKED. "
                    + "Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
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
                    description = "Bank account is not ACTIVE, or a concurrent update prevents blocking"
            )
    })
    @PatchMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> blockAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        BankAccountResponse accountResponse = bankAccountService.blockAccount(principal.getUserId(), id);

        return ResponseEntity.ok(accountResponse);
    }

    @Operation(
            summary = "Unblock bank account",
            description = "Changes a bank account owned by the authenticated user to ACTIVE. "
                    + "Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
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
                    description = "Bank account is not BLOCKED, or a concurrent update prevents unblocking"
            )
    })
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<BankAccountResponse> unblockAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        BankAccountResponse accountResponse = bankAccountService.unblockAccount(principal.getUserId(), id);

        return ResponseEntity.ok(accountResponse);
    }

    @Operation(
            summary = "Close bank account",
            description = """
                    Closes a bank account owned by the authenticated user if its balance is zero.
                    A closed account cannot be reactivated. Requires a valid CSRF token.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
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
                    description = "Bank account is already CLOSED, has a nonzero balance, or a concurrent update prevents closing"
            )
    })
    @PatchMapping("/{id}/close")
    public ResponseEntity<BankAccountResponse> closeAccount(
            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        BankAccountResponse accountResponse = bankAccountService.closeAccount(principal.getUserId(), id);

        return ResponseEntity.ok(accountResponse);
    }

    @Operation(
            summary = "Change bank account outgoing limit",
            description = "Changes the outgoing transfer limit over a rolling 24-hour window for a bank account "
                    + "owned by the authenticated user. The account must be ACTIVE. Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Outgoing transfer limit updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bank account ID or limit amount"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Bank account belongs to another user, or missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bank account is not ACTIVE, or a concurrent update prevents the change"
            )
    })
    @PatchMapping("/{id}/dailyOutgoingLimit")
    public ResponseEntity<BankAccountResponse> changeDailyOutgoingLimit(

            @Parameter(description = "Bank account ID", example = "1")
            @PathVariable
            @Positive
            long id,

            @RequestBody
            @Valid
            ChangeBankAccountDailyOutgoingLimitRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ){
        return ResponseEntity.ok(bankAccountService.changeDailyOutgoingLimit(principal.getUserId(), id, request.amount()));
    }
}
