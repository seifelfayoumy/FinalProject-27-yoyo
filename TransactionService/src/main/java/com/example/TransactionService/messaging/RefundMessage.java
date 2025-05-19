package com.example.TransactionService.messaging;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message for product refund updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private int quantity;
    private String refundId;
} 