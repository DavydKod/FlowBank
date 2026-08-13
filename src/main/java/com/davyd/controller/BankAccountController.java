package com.davyd.controller;

import com.davyd.dto.CreateBankAccountRequest;
import com.davyd.models.BankAccount;
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
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(
            @PathVariable @Positive long id
    ) {
        return ResponseEntity.ok(bankAccountService.getAccountById(id));
    }

    @GetMapping("/by-owner/{ownerId}")
    public ResponseEntity<List<BankAccount>> getAccountsByOwner(
            @PathVariable @Positive long ownerId
    ) {
        return ResponseEntity.ok(
                bankAccountService.getAccountsByOwner(ownerId)
        );
    }

    @PostMapping
    public ResponseEntity<BankAccount> createAccount(
            @RequestBody @Valid CreateBankAccountRequest request
    ) {
        BankAccount account = bankAccountService.createAccount(request.ownerId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable @Positive long id
    ) {
        bankAccountService.deleteAccount(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<BankAccount> blockAccount(
            @PathVariable @Positive long id
    ) {
        BankAccount account = bankAccountService.blockAccount(id);

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<BankAccount> unblockAccount(
            @PathVariable @Positive long id
    ) {
        BankAccount account = bankAccountService.unblockAccount(id);

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<BankAccount> closeAccount(
            @PathVariable @Positive long id
    ) {
        BankAccount account = bankAccountService.closeAccount(id);

        return ResponseEntity.ok(account);
    }
}
