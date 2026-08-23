package com.davyd.service;

import com.davyd.exception.DailyTransferLimitExceededException;
import com.davyd.models.BankAccount;
import com.davyd.repository.TransactionRepository;
import com.davyd.util.Validation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferLimitService {
    private final TransactionRepository transactionRepository;

    public TransferLimitService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    private BigDecimal transferredAmountOfDay(BankAccount account){
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        BigDecimal transferredLast24Hours =
                transactionRepository.getTotalSentSince(
                        account.getId(),
                        since
                );

        return transferredLast24Hours;
    }

    public void validateDailyTransferLimit(BankAccount account, BigDecimal amount){
        account = Validation.validateNotNull(account, "Bank account");
        amount = Validation.validateMoney(amount);

        if (amount.add(transferredAmountOfDay(account))
                .compareTo(account.getDailyTransferLimit()) > 0){
            throw new DailyTransferLimitExceededException("Impossible to transfer money. " +
                    "Daily transfer limit reached");
        }
    }
}
