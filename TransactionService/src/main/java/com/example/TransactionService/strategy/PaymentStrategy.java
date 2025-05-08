package com.example.TransactionService.strategy;


import com.example.TransactionService.model.Transaction;

public interface PaymentStrategy {
    void pay(Transaction tx);
}
