package com.davyd.controller;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.request.CreateBankAccountRequest;
import com.davyd.service.BankAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<Page<BankAccountResponse>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(bankAccountService.getAllAccounts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(
            @PathVariable @Positive long id
    ) {
        return ResponseEntity.ok(bankAccountService.getAccount(id));
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<Page<BankAccountResponse>> getAccountsByOwner(
            @PathVariable @Positive long ownerId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                bankAccountService.getAccountsByOwner(ownerId, pageable)
        );
    }

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @RequestBody @Valid CreateBankAccountRequest request
    ) {
        BankAccountResponse accountResponse = bankAccountService.createAccount(request.ownerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountResponse);
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> blockAccount(
            @PathVariable @Positive long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.blockAccount(id);

        return ResponseEntity.ok(accountResponse);
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<BankAccountResponse> unblockAccount(
            @PathVariable @Positive long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.unblockAccount(id);

        return ResponseEntity.ok(accountResponse);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<BankAccountResponse> closeAccount(
            @PathVariable @Positive long id
    ) {
        BankAccountResponse accountResponse = bankAccountService.closeAccount(id);

        return ResponseEntity.ok(accountResponse);
    }
}
