package com.davyd.integration.service;

import com.davyd.service.BankAccountService;
import com.davyd.service.TransactionService;
import com.davyd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionServiceConcurrencyIT extends BaseServiceIT {
    @Autowired
    private UserService userService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private TransactionService transactionService;
}
