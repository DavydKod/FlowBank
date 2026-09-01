package com.davyd.service;

import com.davyd.dto.response.BankAccountResponse;
import com.davyd.exception.BankAccountNotFoundException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.mapper.BankAccountMapper;
import com.davyd.models.BankAccount;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import com.davyd.util.Validation;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              UserRepository userRepository){
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BankAccountResponse createAccount(long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        BankAccount account = new BankAccount(owner);

        try {
            return BankAccountMapper.toResponse(bankAccountRepository.saveAndFlush(account));
        } catch (DataIntegrityViolationException e){
            if (Validation.isConstraintViolation(e, "fk_bank_accounts_owner")){
                throw new UserNotFoundException(ownerId);
            }

            throw e;
        }
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

    public Page<BankAccountResponse> getAllAccounts(Pageable pageable) {

        return bankAccountRepository.findAll(pageable)
                .map(BankAccountMapper::toResponse);
    }

    public Page<BankAccountResponse> getAccountsByOwner(long ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        return bankAccountRepository.findByOwner_Id(ownerId, pageable)
                .map(BankAccountMapper::toResponse);
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

    @Transactional
    public BankAccountResponse changeDailyOutgoingLimit(long accountId, BigDecimal newLimit){
        Validation.validateMoney(newLimit);

        BankAccount bankAccount = getAccountEntity(accountId);
        bankAccount.changeDailyOutgoingLimit(newLimit);
        return BankAccountMapper.toResponse(bankAccount);
    }
}
