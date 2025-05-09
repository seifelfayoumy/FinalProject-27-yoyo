package com.example.TransactionService.strategy;

import com.example.TransactionService.model.Transaction;
import org.springframework.stereotype.Component;

@Component("CARD")               // bean‑name = paymentMethod
public class CreditCardStrategy implements PaymentStrategy {
    @Override
    public void pay(Transaction tx) {
        // --- pretend you call a credit‑card gateway here ---
        tx.setStatus("PAID");
    }
}

