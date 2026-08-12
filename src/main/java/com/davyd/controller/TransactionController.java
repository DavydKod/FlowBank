package com.davyd.controller;

import com.davyd.dto.CreateTransactionRequest;
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
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable @Positive long id
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionById(id)
        );
    }

    @GetMapping("/by-account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(
            @PathVariable @Positive long accountId
    ) {
        return ResponseEntity.ok(
                transactionService.getTransactionsByAccount(accountId)
        );
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody @Valid CreateTransactionRequest request
    ) {
        Transaction transaction = transactionService.transfer(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }
}
