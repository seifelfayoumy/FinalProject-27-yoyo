package com.example.TransactionService.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.TransactionService.client.PromoCodeClient;
import com.example.TransactionService.client.TokenValidationResponse;
import com.example.TransactionService.client.UserClient;
import com.example.TransactionService.command.CommandInvoker;
import com.example.TransactionService.command.ViewOrderHistoryCommand;
import com.example.TransactionService.messaging.RefundMessage;
import com.example.TransactionService.messaging.RefundPublisher;
import com.example.TransactionService.messaging.StockUpdateMessage;
import com.example.TransactionService.messaging.StockUpdatePublisher;
import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.repository.TransactionRepository;
import com.example.TransactionService.strategy.PaymentStrategy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final PromoCodeClient promoCodeClient;
    private final TransactionRepository repo;
    private final UserClient userClient;
    private final Map<String, PaymentStrategy> paymentStrategies;
    private final CommandInvoker commandInvoker;
    private final ViewOrderHistoryCommand viewOrderHistoryCommand;
    private final StockValidationService stockValidationService;
    private final StockUpdatePublisher stockUpdatePublisher;
    private final RefundPublisher refundPublisher;

    public String applyPromoToCart(String promoCode, Map<UUID, Double> products) {
        // Calculate total from the product map
        double total = products.values().stream().mapToDouble(Double::doubleValue).sum();
        return promoCodeClient.applyPromo(promoCode, total);
    }

    public Transaction create(Transaction tx, String promoCode) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        String token = authHeader.substring(7);

        TokenValidationResponse validationResponse = userClient.validateToken(token);
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to create transactions for other users");
        }

        // Validate stock availability
        if (tx.getProducts() != null && !tx.getProducts().isEmpty()) {
            List<String> insufficientStockProducts = stockValidationService.validateStock(tx.getProducts());
            if (!insufficientStockProducts.isEmpty()) {
                throw new IllegalStateException("Insufficient stock for: " + String.join(", ", insufficientStockProducts));
            }
        }

        //  Apply promo code passed from controller
        if (promoCode != null && !promoCode.isBlank()) {
            try {
                // Calculate total amount from products
                double total = tx.getAmount().doubleValue();
                System.out.println("PROMO DEBUG: Original amount before discount: " + total);
                System.out.println("PROMO DEBUG: Applying promo code: " + promoCode);
                
                // Make the API call to apply promo
                String promoResult = promoCodeClient.applyPromo(promoCode, total);
                System.out.println("PROMO DEBUG: Raw response from promo service: " + promoResult);

                // Check if response contains discount info
                if (promoResult != null && promoResult.contains("New total:")) {
                    // Extract number (discounted amount) from the returned string
                    String[] parts = promoResult.split("New total:");
                    if (parts.length == 2) {
                        String amountStr = parts[1].trim(); // e.g. "82.50"
                        try {
                            BigDecimal discountedAmount = new BigDecimal(amountStr);
                            System.out.println("PROMO DEBUG: Successfully parsed discount amount: " + discountedAmount);
                            
                            // Set the discounted amount and update status
                            tx.setAmount(discountedAmount);
                            tx.setStatus("DISCOUNTED");
                            System.out.println("PROMO DEBUG: Transaction updated with discounted amount");
                        } catch (NumberFormatException e) {
                            System.err.println("PROMO ERROR: Failed to parse discount amount: " + amountStr + " - " + e.getMessage());
                        }
                    } else {
                        System.err.println("PROMO ERROR: Unexpected promo result format: " + promoResult);
                    }
                } else {
                    System.err.println("PROMO ERROR: Response doesn't contain 'New total:' format: " + promoResult);
                }
            } catch (Exception e) {
                System.err.println("PROMO ERROR: Exception while applying promo: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return repo.save(tx);
    }

    public Transaction get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<Transaction> list() {
        return repo.findAll();
    }

    public Transaction update(Long id, Transaction tx) {
        tx.setId(id);
        return repo.save(tx);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Transaction processPayment(Long id) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        String token = authHeader.substring(7);

        Transaction tx = repo.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Transaction not found with id: " + id));
                
        // Check if transaction is already paid
        if ("PAID".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Transaction has already been paid");
        }

        TokenValidationResponse validationResponse = userClient.validateToken(token);
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to process payment for other users' transactions");
        }

        PaymentStrategy strategy = paymentStrategies.getOrDefault(tx.getPaymentMethod(), paymentStrategies.get("CARD"));
        strategy.pay(tx);
        tx.setStatus("PAID");

        Transaction savedTx = repo.save(tx);

        if (tx.getProducts() != null && !tx.getProducts().isEmpty()) {
            for (String productEntry : tx.getProducts()) {
                String[] parts = productEntry.split("\\s+");
                if (parts.length == 2) {
                    try {
                        String productId = parts[0];
                        int quantity = Integer.parseInt(parts[1]);
                        StockUpdateMessage message = StockUpdateMessage.builder()
                                .productId(productId)
                                .quantity(quantity)
                                .transactionId(savedTx.getId().toString())
                                .build();
                        stockUpdatePublisher.sendStockUpdateMessage(message);
                    } catch (Exception e) {
                        System.err.println("Failed to send stock update for product: " + productEntry + " - " + e.getMessage());
                    }
                }
            }
        }

        return savedTx;
    }

    public Transaction refund(Long id) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        String token = authHeader.substring(7);

        Transaction tx = repo.findById(id).orElseThrow();

        TokenValidationResponse validationResponse = userClient.validateToken(token);
        if (!validationResponse.isSuccess()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        if (!validationResponse.getUserId().equals(tx.getUserId())) {
            throw new IllegalArgumentException("You are not authorized to refund transactions for other users");
        }

        if (!"PAID".equalsIgnoreCase(tx.getStatus())) {
            throw new IllegalStateException("Only PAID transactions can be refunded.");
        }

        PaymentStrategy strategy = paymentStrategies.getOrDefault(tx.getPaymentMethod(), paymentStrategies.get("CARD"));
        strategy.refund(tx);
        tx.setStatus("REFUNDED");
        
        // Save the transaction first to ensure it's refunded in our database
        Transaction refundedTx = repo.save(tx);
        
        // Send refund messages for each product to increase stock
        if (tx.getProducts() != null && !tx.getProducts().isEmpty()) {
            for (String productEntry : tx.getProducts()) {
                String[] parts = productEntry.split("\\s+");
                if (parts.length == 2) {
                    try {
                        String productId = parts[0];
                        int quantity = Integer.parseInt(parts[1]);
                        
                        // Create and send refund message
                        RefundMessage message = RefundMessage.builder()
                                .productId(productId)
                                .quantity(quantity)
                                .refundId(refundedTx.getId().toString())
                                .build();
                        
                        refundPublisher.sendRefundMessage(message);
                    } catch (Exception e) {
                        // Log error but continue with other products
                        System.err.println("Failed to send refund message for product: " + productEntry + " - " + e.getMessage());
                    }
                }
            }
        }

        return refundedTx;
    }

    public List<Transaction> getTransactionsByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByOrder(Long orderId) {
        return repo.findByOrderId(orderId);
    }

    public List<Transaction> getUserOrderHistory(Long userId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }
        String token = authHeader.substring(7);

        return commandInvoker.executeCommand(viewOrderHistoryCommand.withParameters(userId, token));
    }
}
