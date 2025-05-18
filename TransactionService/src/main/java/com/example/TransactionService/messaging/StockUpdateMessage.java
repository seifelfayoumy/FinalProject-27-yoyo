package com.example.TransactionService.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message for product stock updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private int quantity;
    private String transactionId;
} 