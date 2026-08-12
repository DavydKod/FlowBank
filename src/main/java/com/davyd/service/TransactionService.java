package com.davyd.service;

import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.models.BankAccount;
import com.davyd.models.Transaction;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.util.Validation;
import jakarta.transaction.Transactional;
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

    @Transactional
    public Transaction transfer(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount
    ) {
        if (fromAccountId == toAccountId){
            throw new IllegalArgumentException("Bank accounts cannot be the same");
        }

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

        amount = Validation.validateBigDecimalNotNullAndPositive(amount);

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        Transaction transaction = new Transaction(
                fromAccount,
                toAccount,
                amount
        );

        return transactionRepository.save(transaction);
    }
}
