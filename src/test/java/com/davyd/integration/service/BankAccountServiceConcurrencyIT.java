package com.davyd.integration.service;

import com.davyd.service.BankAccountService;
import com.davyd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class BankAccountServiceConcurrencyIT extends BaseServiceIT {
    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserService userService;

}
