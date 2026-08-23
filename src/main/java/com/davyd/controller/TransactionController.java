package com.davyd.controller;

import com.davyd.dto.request.CreateTransactionRequest;
import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                transactionService.getAllTransactions(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable @Positive long id
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(id)
        );
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccount(
            @PathVariable @Positive long accountId,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) TransactionSortingMethod sortingMethod,
            @PageableDefault(size = 20) Pageable pageable
            ) {

        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, direction, sortingMethod, pageable));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency key cannot be blank")
            @Size(max = 100, message = "Idempotency key cannot exceed 100 characters")
            String idempotencyKey,
            @RequestBody @Valid CreateTransactionRequest request
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
}
