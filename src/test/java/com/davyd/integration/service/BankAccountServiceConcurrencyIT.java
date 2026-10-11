package com.davyd.integration.service;

import com.davyd.bankaccount.dto.response.BankAccountResponse;
import com.davyd.user.dto.response.UserResponse;
import com.davyd.user.exception.UserNotFoundException;
import com.davyd.bankaccount.BankAccount;
import com.davyd.user.User;
import com.davyd.bankaccount.BankAccountRepository;
import com.davyd.user.UserRepository;
import com.davyd.bankaccount.BankAccountService;
import com.davyd.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static com.davyd.support.ConcurrencyTestUtils.awaitOrThrow;

public class BankAccountServiceConcurrencyIT extends BaseServiceIT {
    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @MockitoSpyBean
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldThrowWhenCreatingConcurrentBankAccountForDeletedUser() throws Exception {
        UserResponse user = userService.createUser("Davyd", "davyd@gmail.com");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch bankAccountCreationCheckCompleted = new CountDownLatch(1);
        CountDownLatch userDeletionCompleted = new CountDownLatch(1);

        AtomicBoolean findCheckInBankAccountCreation = new AtomicBoolean(true);

        clearInvocations(bankAccountRepository, userRepository);

        doAnswer(invocation -> {
            Optional<User> foundUser = userRepository.findByEmail(user.email());
            if (findCheckInBankAccountCreation.get()) {
                bankAccountCreationCheckCompleted.countDown();
                awaitOrThrow(userDeletionCompleted, "User deletion timed out");
            }
            return foundUser;
        }).when(userRepository).findById(user.id());

        try {
            Future<Void> f1 = executor.submit(() -> {
                awaitOrThrow(bankAccountCreationCheckCompleted,"Bank account creation timed out");
                findCheckInBankAccountCreation.set(false);
                userService.deleteUser(user.id());
                userDeletionCompleted.countDown();
                return null;
            });

            Future<BankAccountResponse> f2 = executor.submit(() ->
                    bankAccountService.createAccount(user.id()));

            int successful = 0;
            int failed = 0;

            for (Future<?> future : List.of(f1, f2)) {
                try {
                    future.get(20, TimeUnit.SECONDS);
                    successful++;
                } catch (ExecutionException e) {
                    assertInstanceOf(
                            UserNotFoundException.class,
                            e.getCause()
                    );
                    failed++;
                }
            }

            assertEquals(1, successful);
            assertEquals(1, failed);

            verify(userRepository, times(2))
                    .findById(user.id());

            verify(bankAccountRepository, times(1))
                    .saveAndFlush(any(BankAccount.class));

        } finally {
            executor.shutdownNow();
        }
    }
}
