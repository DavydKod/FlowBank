package com.davyd.service;

import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              UserRepository userRepository){
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    public BankAccount createAccount(long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        BankAccount account = new BankAccount(owner);

        return bankAccountRepository.save(account);
    }

    public BankAccount getAccountById(long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));
    }

    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }

    public void deleteAccount(long id) {
        BankAccount account = getAccountById(id);
        bankAccountRepository.delete(account);
    }

    public List<BankAccount> getAccountsByOwner(long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        return bankAccountRepository.findByOwner_Id(ownerId);
    }

    @Transactional
    public BankAccount blockAccount(long accountId) {
        BankAccount account = getAccountById(accountId);
        account.blockAccount();
        return account;
    }

    @Transactional
    public BankAccount unblockAccount(long accountId) {
        BankAccount account = getAccountById(accountId);
        account.unblockAccount();
        return account;
    }

    @Transactional
    public BankAccount closeAccount(long accountId) {
        BankAccount account = getAccountById(accountId);
        account.closeAccount();
        return account;
    }
}
