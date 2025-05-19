package com.example.AdminService.messaging;

import java.io.Serializable;

/**
 * Message for product stock updates
 */
public class StockUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private int quantity;
    private String transactionId;
    
    // Default constructor for deserialization
    public StockUpdateMessage() {
    }
    
    public StockUpdateMessage(String productId, int quantity, String transactionId) {
        this.productId = productId;
        this.quantity = quantity;
        this.transactionId = transactionId;
    }
    
    // Getters and setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    @Override
    public String toString() {
        return "StockUpdateMessage{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
} 