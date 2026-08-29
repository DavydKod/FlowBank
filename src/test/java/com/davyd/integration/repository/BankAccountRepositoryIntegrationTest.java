package com.davyd.integration.repository;

import com.davyd.exception.UserDeletionNotAllowedException;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.models.AccountStatus;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public class BankAccountRepositoryIntegrationTest {
    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;


    private void flushAndClearPersistenceContext(){
        entityManager.flush();
        entityManager.clear();
    }

    private User createUserForTesting(){
        return new User("Davyd", "davyd@gmail.com");
    }

    @Test
    void shouldSaveAndGetBankAccount(){
        User user = userRepository.save(createUserForTesting());

        BankAccount bankAccount = bankAccountRepository.save(new BankAccount(user));

        flushAndClearPersistenceContext();

        Optional<BankAccount> result = bankAccountRepository.findById(bankAccount.getId());

        assertTrue(result.isPresent());

        BankAccount savedBankAccount = result.get();

        assertNotNull(savedBankAccount.getId());
        assertEquals(user.getId(), savedBankAccount.getOwner().getId());
        assertEquals(AccountStatus.ACTIVE, savedBankAccount.getStatus());
        assertEquals(new BigDecimal("0.00"), savedBankAccount.getBalance());
    }

    @Test
    void shouldCreateMoreAccountsForUser(){
        User user = userRepository.save(createUserForTesting());

        flushAndClearPersistenceContext();

        BankAccount bankAccount1 = bankAccountRepository.save(new BankAccount(user));
        BankAccount bankAccount2 = bankAccountRepository.save(new BankAccount(user));
        BankAccount bankAccount3 = bankAccountRepository.save(new BankAccount(user));

        Page<BankAccount> accounts =
                bankAccountRepository.findByOwner_Id(
                        user.getId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(3, accounts.getTotalElements());
        assertTrue(
                accounts.stream()
                        .allMatch(account ->
                                account.getOwner().getId().equals(user.getId())));
    }

    @Test
    void shouldThrowWhenDeletingUserWithBankAccount(){
        User user = userRepository.save(createUserForTesting());

        bankAccountRepository.save(new BankAccount(user));

        flushAndClearPersistenceContext();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.deleteById(user.getId());
            userRepository.flush();
        });
    }

    @Test
    void shouldExistsByOwnerWhenBankAccountCreated(){
        User user = userRepository.save(createUserForTesting());

        bankAccountRepository.save(new BankAccount(user));

        flushAndClearPersistenceContext();

        assertTrue(bankAccountRepository.existsByOwnerId(user.getId()));
    }

    @Test
    void shouldNotExistByOwnerWhenBankAccountNotCreated(){
        User user = userRepository.save(createUserForTesting());

        flushAndClearPersistenceContext();

        assertFalse(bankAccountRepository.existsByOwnerId(user.getId()));
    }

    @Test
    void shouldNotExistByOwnerWhenUserNotCreated(){
        assertFalse(bankAccountRepository.existsByOwnerId(1L));
    }
}
