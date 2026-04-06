package com.bank.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.model.Account;
import com.bank.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);
    
    @Query("SELECT t FROM Transaction t WHERE t.account.user.id = :userId ORDER BY t.timestamp DESC")
    List<Transaction> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);

        @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.user.id = :userId
              AND (:type IS NULL OR t.type = :type)
              AND (:fromDate IS NULL OR t.timestamp >= :fromDate)
              AND (:toDate IS NULL OR t.timestamp <= :toDate)
            ORDER BY t.timestamp DESC
            """)
        List<Transaction> findFilteredTransactionsByUser(
            @Param("userId") Long userId,
            @Param("type") Transaction.Type type,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}

