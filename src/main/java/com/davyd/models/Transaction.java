package com.davyd.models;

import com.davyd.util.Validation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "from_account_id", nullable = true)
    private BankAccount fromAccount;

    @ManyToOne(optional = true)
    @JoinColumn(name = "to_account_id", nullable = true)
    private BankAccount toAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;


    protected Transaction() {
    }

    public Transaction(TransactionType transactionType, BankAccount fromAccount,
                       BankAccount toAccount, BigDecimal amount, LocalDateTime createdAt, String idempotencyKey) {

        if (transactionType == TransactionType.WITHDRAWAL) {
            Validation.validateNotNull(fromAccount, "Bank account");
            Validation.validateNull(toAccount, "Bank account");
        }

        if (transactionType == TransactionType.DEPOSIT) {
            Validation.validateNotNull(toAccount, "Bank account");
            Validation.validateNull(fromAccount, "Bank account");
        }

        if (transactionType == TransactionType.TRANSFER) {
            Validation.validateNotNull(fromAccount, "Bank account");
            Validation.validateNotNull(toAccount, "Bank account");
        }

        Validation.validateNotBlank(idempotencyKey, "Idempotency key");
        Validation.validateNotNull(createdAt, "Creation date");
        amount = Validation.validateMoney(amount);

        if (idempotencyKey.length() > 100){
            throw new IllegalArgumentException("Idempotency key cannot exceed 100 characters");
        }

        if (fromAccount == toAccount) {
            throw new IllegalArgumentException("Bank accounts must be different for one transaction");
        }

        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() {
        return id;
    }

    public BankAccount getFromAccount() {
        return fromAccount;
    }

    public BankAccount getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
