package com.davyd.controller;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.dto.request.CreateBankAccountRequest;
import com.davyd.service.BankAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(
            @PathVariable @Positive long id
    ) {
        return ResponseEntity.ok(bankAccountService.getAccount(id));
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<List<BankAccountResponse>> getAccountsByOwner(
            @PathVariable @Positive long ownerId
    ) {
        return ResponseEntity.ok(
                bankAccountService.getAccountsByOwner(ownerId)
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable @Positive long id
    ) {
        bankAccountService.deleteAccount(id);

        return ResponseEntity.noContent().build();
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
