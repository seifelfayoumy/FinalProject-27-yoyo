package com.example.TransactionService.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.TransactionService.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Fetch all transactions for a specific user
    List<Transaction> findByUserId(Long userId);

    // Fetch all transactions related to a specific order
    List<Transaction> findByOrderId(Long orderId);

}