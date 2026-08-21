package com.davyd.repository;

import com.davyd.models.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccount_Id(Long accountId, Sort sort);
    List<Transaction> findByToAccount_Id(Long accountId, Sort sort);
    List<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Sort sort);

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
