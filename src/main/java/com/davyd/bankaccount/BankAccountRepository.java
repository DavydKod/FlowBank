package com.davyd.bankaccount;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Page<BankAccount> findByOwner_Id(Long ownerId, Pageable pageable);
    boolean existsByOwnerId(Long ownerId);

    @Query("""
        select a.owner.id
        from BankAccount a
        where a.id = :accountId
        """)
    Optional<Long> findOwnerIdByAccountId(
            @Param("accountId") long accountId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.id = :id")
    Optional<BankAccount> findByIdForUpdate(@Param("id") Long id);
}
