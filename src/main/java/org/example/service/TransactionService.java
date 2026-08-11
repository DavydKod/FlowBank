package org.example.service;

import org.example.exception.BankAccountNotFoundException;
import org.example.exception.TransactionNotFoundException;
import org.example.models.BankAccount;
import org.example.models.Transaction;
import org.example.repository.BankAccountRepository;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            BankAccountRepository bankAccountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public Transaction getTransactionById(long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByAccount(long accountId) {
        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException(accountId);
        }

        return transactionRepository
                .findByFromAccount_IdOrToAccount_Id(
                        accountId,
                        accountId
                );
    }

    public Transaction createTransaction(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount
    ) {
        BankAccount fromAccount = bankAccountRepository
                .findById(fromAccountId)
                .orElseThrow(
                        () -> new BankAccountNotFoundException(fromAccountId)
                );

        BankAccount toAccount = bankAccountRepository
                .findById(toAccountId)
                .orElseThrow(
                        () -> new BankAccountNotFoundException(toAccountId)
                );

        Transaction transaction = new Transaction(
                fromAccount,
                toAccount,
                amount
        );

        return transactionRepository.save(transaction);
    }
}
