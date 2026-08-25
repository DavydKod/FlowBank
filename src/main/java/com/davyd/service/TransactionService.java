package com.davyd.service;

import com.davyd.dto.TransactionDirection;
import com.davyd.dto.TransactionSortingMethod;
import com.davyd.dto.response.TransactionResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.IdempotencyKeyConflictException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.exception.TransactionNotFoundException;
import com.davyd.mapper.TransactionMapper;
import com.davyd.models.AccountStatus;
import com.davyd.models.BankAccount;
import com.davyd.models.Transaction;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.util.Validation;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransferLimitService transferLimitService;
    private final Clock clock;

    public TransactionService(
            TransactionRepository transactionRepository,
            BankAccountRepository bankAccountRepository,
            TransferLimitService transferLimitService,
            Clock clock
    ) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transferLimitService = transferLimitService;
        this.clock = clock;
    }

    public TransactionResponse getTransactionById(long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return TransactionMapper.toResponse(transaction);
    }

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(TransactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByAccount(long accountId, TransactionDirection direction,
                                                      TransactionSortingMethod sortingMethod, Pageable pageable) {
        validateAccountExists(accountId);

        Sort sort = getSortMethod(sortingMethod);

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(), sort);

        if (direction == TransactionDirection.FROM) {
            return transactionRepository.findByFromAccount_Id(accountId, sortedPageable)
                    .map(TransactionMapper::toResponse);
        }

        if (direction == TransactionDirection.TO) {
            return transactionRepository.findByToAccount_Id(accountId, sortedPageable)
                    .map(TransactionMapper::toResponse);
        }

        return transactionRepository
                .findByFromAccount_IdOrToAccount_Id(
                        accountId,
                        accountId,
                        sortedPageable
                ).map(TransactionMapper::toResponse);
    }

    private void validateAccountExists(long accountId) {
        if (!bankAccountRepository.existsById(accountId)) {
            throw new BankAccountNotFoundException(accountId);
        }
    }

    @Transactional
    public TransactionResponse transfer(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        amount = Validation.validateMoney(amount);
        idempotencyKey = Validation.validateNotBlank(idempotencyKey, "Idempotency key");

        if (idempotencyKey.length() > 100){
            throw new IllegalArgumentException("Idempotency key cannot exceed 100 characters");
        }

        if (fromAccountId == toAccountId){
            throw new IllegalArgumentException("Bank accounts cannot be the same");
        }

        Optional<Transaction> existingTransactionWithKey = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransactionWithKey.isPresent()){
            return handleExisting(existingTransactionWithKey.get(), fromAccountId, toAccountId, amount);
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

        existingTransactionWithKey = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTransactionWithKey.isPresent()){
            return handleExisting(existingTransactionWithKey.get(), fromAccountId, toAccountId, amount);
        }

        transferLimitService.validateDailyTransferLimit(fromAccount, amount);

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        Transaction transaction = new Transaction(
                fromAccount,
                toAccount,
                amount,
                LocalDateTime.now(clock),
                idempotencyKey
        );

        return TransactionMapper
                .toResponse(transactionRepository.save(transaction));
    }

    private Sort getSortMethod(TransactionSortingMethod method){
        if (method == null){
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (method){
            case CREATED_AT_ASC ->  Sort.by(Sort.Direction.ASC, "createdAt");
            case CREATED_AT_DESC -> Sort.by(Sort.Direction.DESC, "createdAt");
            case AMOUNT_DESC -> Sort.by(Sort.Direction.DESC, "amount");
            case AMOUNT_ASC -> Sort.by(Sort.Direction.ASC, "amount");
        };
    }

    private boolean matchesRequest(Transaction transaction, Long fromId, Long toId, BigDecimal amount){
        return  Objects.equals(transaction.getFromAccount().getId(), fromId) &&
                Objects.equals(transaction.getToAccount().getId(), toId) &&
                transaction.getAmount().compareTo(amount) == 0;
    }

    private TransactionResponse handleExisting(Transaction transaction, Long fromAccountId, Long toAccountId, BigDecimal amount){
        if (!matchesRequest(transaction, fromAccountId, toAccountId, amount)){
            throw new IdempotencyKeyConflictException("Idempotency key was already used for another transfer");
        }

        return TransactionMapper.toResponse(transaction);
    }
}
