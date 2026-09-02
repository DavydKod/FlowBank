package com.davyd.integration.service;

import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.TransactionRepository;
import com.davyd.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class BaseServiceIT {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanDatabase(){
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
    }
}
