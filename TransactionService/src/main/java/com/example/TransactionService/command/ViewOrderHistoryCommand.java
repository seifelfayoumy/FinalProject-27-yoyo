package com.example.TransactionService.command;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.TransactionService.client.TokenValidationResponse;
import com.example.TransactionService.client.UserClient;
import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Command to retrieve order history for a specific user.
 * Implements token validation to ensure the user can only view their own transactions.
 */
@Component
@RequiredArgsConstructor
public class ViewOrderHistoryCommand implements Command<List<Transaction>> {

    private final TransactionRepository transactionRepository;
    private final UserClient userClient;
    
    private Long userId;
    private String token;
    
    /**
     * Sets the parameters for the command.
     * 
     * @param userId The ID of the user whose order history is being requested
     * @param token The authentication token of the requesting user
     * @return This command instance for method chaining
     */
    public ViewOrderHistoryCommand withParameters(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        return this;
    }
    
    @Override
    public List<Transaction> execute() {
        // Validate that the token is valid and belongs to the requested user
        TokenValidationResponse validationResponse = userClient.validateToken(token);
        
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        // Ensure the authenticated user can only view their own transactions
        if (!validationResponse.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to view transactions for other users");
        }
        
        // Retrieve and return the transaction history
        return transactionRepository.findByUserId(userId);
    }
} 