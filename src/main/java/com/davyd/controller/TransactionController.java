package com.davyd.controller;

import com.davyd.dto.request.CreateTransactionRequest;
import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.models.Transaction;
import com.davyd.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(
                transactionService.getAllTransactions()
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
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @PathVariable @Positive long accountId,
            @RequestParam(required = false) TransactionDirection direction,
            @RequestParam(required = false) TransactionSortingMethod sortingMethod
            ) {

        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, direction, sortingMethod));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
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
