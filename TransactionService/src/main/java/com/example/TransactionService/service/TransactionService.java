package com.example.TransactionService.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.TransactionService.client.TokenValidationResponse;
import com.example.TransactionService.client.UserClient;
import com.example.TransactionService.command.CommandInvoker;
import com.example.TransactionService.command.ViewOrderHistoryCommand;
import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.repository.TransactionRepository;
import com.example.TransactionService.strategy.PaymentStrategy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository repo;
    private final UserClient userClient;
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final CommandInvoker commandInvoker;
    private final ViewOrderHistoryCommand viewOrderHistoryCommand;

    public Transaction create(Transaction tx) {
        // Get the token from Authorization header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token and get user ID
        TokenValidationResponse validationResponse = userClient.validateToken(token);
        
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        // Ensure the token's user ID matches the transaction's user ID
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to create transactions for other users");
        }
        
        // 2. proceed with saving
        return repo.save(tx);
    }
    
    public Transaction get(Long id)                         { return repo.findById(id).orElseThrow(); }
    public List<Transaction> list()                         { return repo.findAll(); }
    public Transaction update(Long id, Transaction tx)      { tx.setId(id); return repo.save(tx); }
    public void delete(Long id)                             { repo.deleteById(id); }
    public Transaction processPayment(Long id) {
        // Get the token from Authorization header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        Transaction tx = repo.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("Transaction not found with id: " + id));
        
        // Validate token and get user ID
        TokenValidationResponse validationResponse = userClient.validateToken(token);
        
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        // Ensure the token's user ID matches the transaction's user ID
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to process payment for other users' transactions");
        }

        PaymentStrategy strategy =
                paymentStrategies.getOrDefault(tx.getPaymentMethod(), paymentStrategies.get("CARD"));

        strategy.pay(tx);
        return repo.save(tx);
    }
    
    public Transaction refund(Long id) {
        // Get the token from Authorization header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        Transaction tx = repo.findById(id).orElseThrow();
        
        // Validate token and get user ID
        TokenValidationResponse validationResponse = userClient.validateToken(token);
        
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        // Ensure the token's user ID matches the transaction's user ID
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to refund transactions for other users");
        }

        if (!"PAID".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Only PAID transactions can be refunded.");
        }

        PaymentStrategy strategy = paymentStrategies.getOrDefault(tx.getPaymentMethod(), paymentStrategies.get("CARD"));

        strategy.refund(tx);
        tx.setStatus("REFUNDED");

        return repo.save(tx);
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByOrder(Long orderId) {
        return repo.findByOrderId(orderId);
    }

    public List<Transaction> getUserOrderHistory(Long userId) {
        // Get the token from Authorization header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Using Command pattern to fetch the user's order history
        return commandInvoker.executeCommand(
            viewOrderHistoryCommand.withParameters(userId, token)
        );
    }
}

