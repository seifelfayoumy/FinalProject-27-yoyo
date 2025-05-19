package com.example.TransactionService.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for publishing refund messages to RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key.refund}")
    private String refundRoutingKey;
    
    /**
     * Sends a refund message to RabbitMQ
     * 
     * @param message Refund message containing product ID and quantity to increase
     */
    public void sendRefundMessage(RefundMessage message) {
        log.info("Sending refund message: {}", message);
        rabbitTemplate.convertAndSend(exchangeName, refundRoutingKey, message);
        log.info("Refund message sent successfully");
    }
} 