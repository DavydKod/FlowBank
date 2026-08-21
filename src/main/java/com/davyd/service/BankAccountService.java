package com.davyd.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.mapper.BankAccountMapper;
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

    public BankAccountResponse createAccount(long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        BankAccount account = new BankAccount(owner);

        return BankAccountMapper.toResponse(bankAccountRepository.save(account));
    }

    public BankAccountResponse getAccount(long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        return BankAccountMapper.toResponse(account);
    }

    private BankAccount getAccountEntity(long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));
    }

    public List<BankAccountResponse> getAllAccounts() {

        return bankAccountRepository.findAll()
                .stream().map(BankAccountMapper::toResponse).toList();
    }

    public List<BankAccountResponse> getAccountsByOwner(long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        return bankAccountRepository.findByOwner_Id(ownerId).stream()
                .map(BankAccountMapper::toResponse).toList();
    }

    @Transactional
    public BankAccountResponse blockAccount(long accountId) {
        BankAccount account = getAccountEntity(accountId);
        account.blockAccount();
        return BankAccountMapper.toResponse(account);
    }

    @Transactional
    public BankAccountResponse unblockAccount(long accountId) {
        BankAccount account = getAccountEntity(accountId);
        account.unblockAccount();
        return BankAccountMapper.toResponse(account);
    }

    @Transactional
    public BankAccountResponse closeAccount(long accountId) {
        BankAccount account = getAccountEntity(accountId);
        account.closeAccount();
        return BankAccountMapper.toResponse(account);
    }
}
