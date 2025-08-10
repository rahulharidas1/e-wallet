package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Wallet findByUserId(String userId);

    @Modifying
    @Transactional
    @Query("update Wallet w set w.amount = w.amount + :amount where w.userId = :userId")
    void updateWallet(String userId, int amount);
}
