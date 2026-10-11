package com.davyd.bankaccount;

import com.davyd.bankaccount.exception.DailyTransferLimitExceededException;
import com.davyd.transaction.TransactionRepository;
import com.davyd.common.util.Validation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class OutgoingTransactionsLimitService {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public OutgoingTransactionsLimitService(TransactionRepository transactionRepository, Clock clock){
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    private BigDecimal transferredAmountOfDay(BankAccount account){
        LocalDateTime since = LocalDateTime.now(clock).minusHours(24);

        BigDecimal transferredLast24Hours =
                transactionRepository.getTotalSentSince(
                        account.getId(),
                        since
                );

        return transferredLast24Hours;
    }

    // Daily limit as a 24-h rolling window, not a calendar day
    // Including external deposits
    public void validateDailyTransferLimit(BankAccount account, BigDecimal amount){
        account = Validation.validateNotNull(account, "Bank account");
        amount = Validation.validateMoney(amount);

        if (amount.add(transferredAmountOfDay(account))
                .compareTo(account.getDailyOutgoingLimit()) > 0){
            throw new DailyTransferLimitExceededException("Impossible to transfer money. " +
                    "Daily transfer limit reached");
        }
    }
}
