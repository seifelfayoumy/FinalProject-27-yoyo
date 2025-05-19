package com.example.AdminService.messaging;

import java.io.Serializable;

/**
 * Message for product refund updates
 */
public class RefundMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private int quantity;
    private String refundId;
    
    // Default constructor for deserialization
    public RefundMessage() {
    }
    
    public RefundMessage(String productId, int quantity, String refundId) {
        this.productId = productId;
        this.quantity = quantity;
        this.refundId = refundId;
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
    
    public String getRefundId() {
        return refundId;
    }
    
    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }
    
    @Override
    public String toString() {
        return "RefundMessage{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", refundId='" + refundId + '\'' +
                '}';
    }
} 