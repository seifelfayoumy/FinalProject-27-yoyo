package com.example.TransactionService.service;

import java.util.*;

import com.example.TransactionService.client.PromoCodeClient;
import com.example.TransactionService.client.TokenValidationResponse;
import com.example.TransactionService.client.UserClient;
import com.example.TransactionService.command.CommandInvoker;
import com.example.TransactionService.command.ViewOrderHistoryCommand;
import com.example.TransactionService.messaging.StockUpdateMessage;
import com.example.TransactionService.messaging.StockUpdatePublisher;
import com.example.TransactionService.model.Transaction;
import com.example.TransactionService.repository.TransactionRepository;
import com.example.TransactionService.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    public String applyPromoToCart(String promoCode, Map<UUID, Double> products) {
        return promoCodeClient.applyPromo(promoCode, products);
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
                Map<UUID, Double> productMap = new HashMap<>();
                for (String entry : tx.getProducts()) {
                    String[] parts = entry.split("\\s+");
                    if (parts.length == 2) {
                        UUID productId = UUID.fromString(parts[0]);
                        double price = Double.parseDouble(parts[1]);
                        productMap.put(productId, price);
                    }
                }

                String promoResult = promoCodeClient.applyPromo(promoCode, productMap);
                System.out.println("Promo applied: " + promoResult);
                tx.setStatus("DISCOUNTED");

            } catch (Exception e) {
                System.err.println("Failed to apply promo: " + e.getMessage());
            }
        }

        return repo.save(tx);
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
