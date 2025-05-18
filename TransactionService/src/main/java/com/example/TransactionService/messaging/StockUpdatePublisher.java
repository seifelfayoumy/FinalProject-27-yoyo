package com.example.TransactionService.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for publishing stock update messages to RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdatePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key.stock-update}")
    private String stockUpdateRoutingKey;
    
    /**
     * Sends a stock update message to RabbitMQ
     * 
     * @param message Stock update message containing product ID and quantity to decrease
     */
    public void sendStockUpdateMessage(StockUpdateMessage message) {
        log.info("Sending stock update message: {}", message);
        rabbitTemplate.convertAndSend(exchangeName, stockUpdateRoutingKey, message);
        log.info("Stock update message sent successfully");
    }
} 