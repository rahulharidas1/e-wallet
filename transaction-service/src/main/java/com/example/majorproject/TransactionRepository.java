package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("update Transaction t set t.transactionStatus = :transactionStatus where t.transactionId = :transactionId")
    void updateTransactionByStatus(TransactionStatus transactionStatus, String transactionId);
}
