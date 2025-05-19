package com.example.AdminService.messaging;

import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.AdminService.model.Product;
import com.example.AdminService.service.ProductService;

/**
 * Consumer for refund messages from RabbitMQ
 */
@Component
public class RefundConsumer {

    private static final Logger logger = Logger.getLogger(RefundConsumer.class.getName());
    
    @Autowired
    private ProductService productService;
    
    /**
     * Listens for refund messages and processes them to increase stock
     * 
     * @param message The refund message from RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.refund}", autoStartup = "true")
    public void handleRefund(RefundMessage message) {
        try {
            logger.info("Received refund message: " + message);
            
            if (message.getProductId() == null || message.getProductId().isEmpty()) {
                logger.warning("Invalid product ID received in refund message: " + message);
                throw new AmqpRejectAndDontRequeueException("Invalid product ID in refund message");
            }
            
            UUID productId;
            try {
                productId = UUID.fromString(message.getProductId());
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid UUID format for product ID in refund: " + message.getProductId());
                throw new AmqpRejectAndDontRequeueException("Invalid product ID format: " + message.getProductId());
            }
            
            // Process the refund by increasing stock
            Product updatedProduct = productService.increaseStock(message.getProductId(), message.getQuantity());
            
            logger.info("Successfully processed refund for product: " + productId + 
                    " (New quantity: " + updatedProduct.getQuantity() + ")");
            
        } catch (IllegalArgumentException e) {
            logger.warning("Error processing refund: " + e.getMessage());
            // Reject but don't requeue - permanent failure for this message
            throw new AmqpRejectAndDontRequeueException("Error processing refund: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Unexpected error processing refund message: " + e.getMessage());
            e.printStackTrace();
            // For unexpected errors, reject but allow requeuing
            throw e;
        }
    }
} 