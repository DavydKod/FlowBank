package com.davyd.repository;

import com.davyd.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByFromAccount_Id(Long accountId, Pageable pageable);
    Page<Transaction> findByToAccount_Id(Long accountId, Pageable pageable);
    Page<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Pageable pageable);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.fromAccount.id = :accountId
    """)
    BigDecimal getTotalSent(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.toAccount.id = :accountId
    """)
    BigDecimal getTotalReceived(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT COUNT(t)
    FROM Transaction t
    WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId
    """)
    long getTransactionCount(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT COUNT(t)
    FROM Transaction t
    WHERE t.fromAccount.id = :accountId
    """)
    long getSentTransactionCount(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT COUNT(t)
    FROM Transaction t
    WHERE t.toAccount.id = :accountId
    """)
    long getReceivedTransactionCount(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT MAX(t.amount)
    FROM Transaction t
    WHERE t.fromAccount.id = :accountId
    """)
    BigDecimal getLargestSentTransaction(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT MAX(t.amount)
    FROM Transaction t
    WHERE t.toAccount.id = :accountId
    """)
    BigDecimal getLargestReceivedTransaction(@Param("accountId") Long bankAccountId);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.fromAccount.id = :accountId
      AND t.createdAt >= :since
    """)
    BigDecimal getTotalSentSince(
            @Param("accountId") Long accountId,
            @Param("since") LocalDateTime since
    );

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
