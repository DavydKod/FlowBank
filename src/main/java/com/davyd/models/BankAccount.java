package com.davyd.models;

import com.davyd.exception.InsufficientFundsException;
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


    protected BankAccount() {
    }

    public BankAccount(User owner) {
        this.owner = owner;
        this.balance = BigDecimal.ZERO;
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

    public void deposit(BigDecimal amount) {
        balance = balance.add(Validation.validateBigDecimalNotNullAndPositive(amount));
    }

    public void withdraw(BigDecimal amount) {
        amount = Validation.validateBigDecimalNotNullAndPositive(amount);

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        balance = balance.subtract(amount);
    }
}
