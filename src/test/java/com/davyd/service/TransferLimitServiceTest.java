package com.davyd.service;

import com.davyd.exception.DailyTransferLimitExceededException;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferLimitServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferLimitService transferLimitService;

    @Test
    void shouldRejectTransferWhenDailyLimitWouldBeExceeded() {
        User user = new User("Joel", "joel@gmail.com");

        BankAccount account = new BankAccount(user);

        BigDecimal alreadyTransferred = new BigDecimal("900.00");
        BigDecimal newTransferAmount = new BigDecimal("150.00");

        when(
                transactionRepository.getTotalSentSince(
                        eq(account.getId()),
                        any(LocalDateTime.class)
                )
        ).thenReturn(alreadyTransferred);

        assertThrows(
                DailyTransferLimitExceededException.class,
                () -> transferLimitService.validateDailyTransferLimit(
                        account,
                        newTransferAmount
                )
        );
    }

    @Test
    void shouldAllowTransferWhenDailyLimitNotExceeded() {
        User user = new User("Joel", "joel@gmail.com");

        BankAccount account = new BankAccount(user);

        BigDecimal alreadyTransferred = new BigDecimal("800.00");
        BigDecimal newTransferAmount = new BigDecimal("150.00");

        when(
                transactionRepository.getTotalSentSince(
                        eq(account.getId()),
                        any(LocalDateTime.class)
                )
        ).thenReturn(alreadyTransferred);

        transferLimitService.validateDailyTransferLimit(account, newTransferAmount);
    }

    @Test
    void shouldAllowTransferWhenDailyLimitNotExceededOnEdgeCase() {
        User user = new User("Joel", "joel@gmail.com");

        BankAccount account = new BankAccount(user);

        BigDecimal alreadyTransferred = new BigDecimal("850.00");
        BigDecimal newTransferAmount = new BigDecimal("150.00");

        when(
                transactionRepository.getTotalSentSince(
                        eq(account.getId()),
                        any(LocalDateTime.class)
                )
        ).thenReturn(alreadyTransferred);

        transferLimitService.validateDailyTransferLimit(account, newTransferAmount);
    }

    @Test
    void shouldThrowWhenBigDecimalNull(){
        User user = new User("Joel", "joel@gmail.com");

        BankAccount account = new BankAccount(user);

        assertThrows(IllegalArgumentException.class, () ->
                transferLimitService.validateDailyTransferLimit(account, null));
    }

    @Test
    void shouldThrowWhenBankAccountNull(){
        assertThrows(IllegalArgumentException.class, () ->
                transferLimitService.validateDailyTransferLimit(null, BigDecimal.valueOf(10)));
    }

    @Test
    void shouldThrowWhenBigDecimalExceededCharacterLimit(){
        User user = new User("Joel", "joel@gmail.com");

        BankAccount account = new BankAccount(user);

        assertThrows(IllegalArgumentException.class, () ->
                transferLimitService.validateDailyTransferLimit(account, BigDecimal.valueOf(100000000000000000L)));
    }
}