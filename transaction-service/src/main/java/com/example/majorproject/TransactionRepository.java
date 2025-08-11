package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Transactional
    @Modifying
    @Query("update Transaction t set t.transactionStatus = :transactionStatus where t.transactionId = :transactionId")
    void updateTransactionByStatus(TransactionStatus transactionStatus, String transactionId);
}
