package com.davyd.repository;

import com.davyd.models.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccount_Id(Long accountId, Sort sort);
    List<Transaction> findByToAccount_Id(Long accountId, Sort sort);
    List<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Sort sort);
}
