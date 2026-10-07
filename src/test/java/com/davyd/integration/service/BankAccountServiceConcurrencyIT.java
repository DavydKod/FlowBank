package com.davyd.integration.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.exception.UserDeletionNotAllowedException;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import com.davyd.service.BankAccountService;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void shouldThrowWhenDeletingConcurrentUserWithCreatedBankAccount() throws Exception{
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch accountCheckReached  = new CountDownLatch(1);
        CountDownLatch accountCreationShouldBegin = new CountDownLatch(1);

        Long userId = userService.createUser("Davyd", "davyd@gmail.com").id();

        doAnswer(invocation -> {
            // Force both requests past the preliminary uniqueness check
            // so the database constraint resolves the race
            accountCreationShouldBegin.countDown();
            accountCheckReached.await(5, TimeUnit.SECONDS);
            return false;
        }).when(bankAccountRepository).existsByOwnerId(userId);

        try {
            Future<?> f1 = executor.submit(() -> {
                userService.deleteUser(userId);
                return null;
            });

            Future<BankAccountResponse> f2 = executor.submit(()-> {
                accountCreationShouldBegin.await(5, TimeUnit.SECONDS);
                BankAccountResponse bankAccountResponse = bankAccountService.createAccount(userId);
                accountCheckReached.countDown();
                return bankAccountResponse;
            });

            int successful = 0;
            int failed = 0;

            for (Future<?> future: List.of(f1, f2)){
                try{
                    future.get();
                    successful++;
                } catch (ExecutionException e){
                    assertInstanceOf(
                            UserDeletionNotAllowedException.class,
                            e.getCause()
                    );
                    failed++;
                }
            }

            assertEquals(1, successful);
            assertEquals(1, failed);

            verify(userRepository, times(1))
                    .delete(any(User.class));

            verify(bankAccountRepository, times(1))
                    .saveAndFlush(any(BankAccount.class));

        } finally {
            executor.shutdownNow();
        }
    }
}
