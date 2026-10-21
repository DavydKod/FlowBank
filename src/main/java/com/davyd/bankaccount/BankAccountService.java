package com.davyd.bankaccount;

import com.davyd.bankaccount.dto.response.BankAccountResponse;
import com.davyd.bankaccount.exception.BankAccountNotFoundException;
import com.davyd.security.authorization.OwnershipValidator;
import com.davyd.user.exception.UserNotFoundException;
import com.davyd.user.User;
import com.davyd.user.UserRepository;
import com.davyd.common.util.Validation;
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
    private final OwnershipValidator ownershipValidator;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              UserRepository userRepository,
                              OwnershipValidator ownershipValidator){
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.ownershipValidator = ownershipValidator;
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

    public BankAccountResponse getAccount(long currentUserId, long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));

        ownershipValidator.requireOwner(currentUserId, account.getOwner().getId());

        return BankAccountMapper.toResponse(account);
    }

    private BankAccount getAccountEntity(long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException(id));
    }

    public Page<BankAccountResponse> getAccountsByOwner(long ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        return bankAccountRepository.findByOwner_Id(ownerId, pageable)
                .map(BankAccountMapper::toResponse);
    }

    @Transactional
    public BankAccountResponse blockAccount(long currentUserId, long accountId) {
        BankAccount account = getAccountEntity(accountId);

        ownershipValidator.requireOwner(currentUserId, account.getOwner().getId());

        account.blockAccount();
        return BankAccountMapper.toResponse(account);
    }

    @Transactional
    public BankAccountResponse unblockAccount(long currentUserId, long accountId) {
        BankAccount account = getAccountEntity(accountId);

        ownershipValidator.requireOwner(currentUserId, account.getOwner().getId());

        account.unblockAccount();
        return BankAccountMapper.toResponse(account);
    }

    @Transactional
    public BankAccountResponse closeAccount(long currentUserId, long accountId) {
        BankAccount account = getAccountEntity(accountId);

        ownershipValidator.requireOwner(currentUserId, account.getOwner().getId());

        account.closeAccount();
        return BankAccountMapper.toResponse(account);
    }

    @Transactional
    public BankAccountResponse changeDailyOutgoingLimit(long currentUserId, long accountId, BigDecimal newLimit){
        Validation.validateMoney(newLimit);

        BankAccount bankAccount = getAccountEntity(accountId);

        ownershipValidator.requireOwner(currentUserId, bankAccount.getOwner().getId());

        bankAccount.changeDailyOutgoingLimit(newLimit);
        return BankAccountMapper.toResponse(bankAccount);
    }
}
