package com.example.AdminService.messaging;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.AdminService.model.Product;
import com.example.AdminService.service.ProductService;

/**
 * Consumer for stock update messages from RabbitMQ
 */
@Component
public class StockUpdateConsumer {

    private static final Logger logger = Logger.getLogger(StockUpdateConsumer.class.getName());
    
    @Autowired
    private ProductService productService;
    
    /**
     * Listens for stock update messages and processes them
     * 
     * @param message The stock update message from RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.stock-update}", autoStartup = "true")
    public void handleStockUpdate(StockUpdateMessage message) {
        try {
            logger.info("Received stock update message: " + message);
            
            if (message.getProductId() == null || message.getProductId().isEmpty()) {
                logger.warning("Invalid product ID received in message: " + message);
                throw new AmqpRejectAndDontRequeueException("Invalid product ID in message");
            }
            
            UUID productId;
            try {
                productId = UUID.fromString(message.getProductId());
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid UUID format for product ID: " + message.getProductId());
                throw new AmqpRejectAndDontRequeueException("Invalid product ID format: " + message.getProductId());
            }
            
            // Process the stock update
            Optional<Product> updatedProduct = productService.decreaseProductQuantity(productId, message.getQuantity());
            
            if (updatedProduct.isPresent()) {
                logger.info("Successfully decreased stock for product: " + productId + 
                        " (New quantity: " + updatedProduct.get().getQuantity() + ")");
            } else {
                logger.warning("Could not find product with ID: " + productId);
                throw new AmqpRejectAndDontRequeueException("Product not found: " + productId);
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Error processing stock update: " + e.getMessage());
            // Reject but don't requeue - permanent failure for this message
            throw new AmqpRejectAndDontRequeueException("Error processing stock update: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.severe("Unexpected error processing stock update message: " + e.getMessage());
            e.printStackTrace();
            // For unexpected errors, reject but allow requeuing
            throw e;
        }
    }
} 