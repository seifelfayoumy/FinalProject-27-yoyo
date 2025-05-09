package com.example.TransactionService.strategy;

import com.example.TransactionService.model.Transaction;
import org.springframework.stereotype.Component;

@Component("WALLET")
public class WalletStrategy implements PaymentStrategy {
    @Override
    public void pay(Transaction tx) {
        // --- pretend you debit the user’s wallet ---
        tx.setStatus("PAID");
    }
}

