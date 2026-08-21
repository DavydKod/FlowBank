package com.davyd.models;

import com.davyd.exception.InsufficientFundsException;
import com.davyd.exception.InvalidAccountStatusException;
import com.davyd.util.Validation;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "daily_transfer_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyTransferLimit;

    @Version
    @Column(nullable = false)
    private Long version;

    private static final BigDecimal DEFAULT_DAILY_TRANSFER_LIMIT = new BigDecimal(1000);

    protected BankAccount() {
    }

    public BankAccount(User owner) {
        this.owner = owner;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
        this.dailyTransferLimit = DEFAULT_DAILY_TRANSFER_LIMIT;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getDailyTransferLimit() {
        return dailyTransferLimit;
    }

    public void deposit(BigDecimal amount) {
        validateActive();
        balance = balance.add(Validation.validateMoney(amount));
    }

    public void withdraw(BigDecimal amount) {
        validateActive();
        amount = Validation.validateMoney(amount);

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        balance = balance.subtract(amount);
    }

    private void validateActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Bank account must be active to perform operations");
        }
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void blockAccount() {
        if (status != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Only active account can be blocked");
        }

        status = AccountStatus.BLOCKED;
    }

    public void unblockAccount() {
        if (status != AccountStatus.BLOCKED) {
            throw new InvalidAccountStatusException("Only blocked account can be unblocked");
        }

        status = AccountStatus.ACTIVE;
    }

    public void closeAccount() {
        if (status == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Account is already closed");
        } else if (balance.compareTo(BigDecimal.ZERO) != 0){
            throw new InvalidAccountStatusException("To close the account it needs to have 0 on balance");
        }

        status = AccountStatus.CLOSED;
    }
}
