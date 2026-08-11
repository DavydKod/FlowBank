package com.davyd.repository;

import com.davyd.models.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByOwner_Id(Long ownerId);
}
