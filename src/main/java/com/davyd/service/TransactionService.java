package com.davyd.service;

import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.models.AccountStatus;
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
        amount = Validation.validateBigDecimalNotNullAndPositive(amount);

        if (fromAccountId == toAccountId){
            throw new IllegalArgumentException("Bank accounts cannot be the same");
        }

        //to avoid deadlocks
        long firstId = Math.min(fromAccountId, toAccountId);
        long secondId = Math.max(fromAccountId, toAccountId);

        BankAccount firstAccount = bankAccountRepository
                .findByIdForUpdate(firstId)
                .orElseThrow(
                        () -> new BankAccountNotFoundException(firstId)
                );

        BankAccount secondAccount = bankAccountRepository
                .findByIdForUpdate(secondId)
                .orElseThrow(
                        () -> new BankAccountNotFoundException(secondId)
                );

        BankAccount fromAccount = fromAccountId == firstId ? firstAccount : secondAccount;
        BankAccount toAccount = toAccountId == firstId ? firstAccount : secondAccount;

        if (fromAccount.getStatus() != AccountStatus.ACTIVE || toAccount.getStatus() != AccountStatus.ACTIVE){
            throw new InvalidAccountStatusException("Status of account must be active to provide transaction");
        }

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
