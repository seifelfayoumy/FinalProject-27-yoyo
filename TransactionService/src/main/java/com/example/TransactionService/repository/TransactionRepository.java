package com.example.TransactionService.repository;

import com.example.TransactionService.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Fetch all transactions for a specific user
    List<Transaction> findByUserId(Long userId);

    // Fetch all transactions related to a specific order
    List<Transaction> findByOrderId(Long orderId);
  
}
