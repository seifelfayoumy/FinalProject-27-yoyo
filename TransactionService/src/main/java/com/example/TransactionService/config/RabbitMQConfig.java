package com.example.TransactionService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.queue.stock-update}")
    private String stockUpdateQueueName;
    
    @Value("${rabbitmq.routing.key.stock-update}")
    private String stockUpdateRoutingKey;
    
    // Exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }
    
    // Queue for stock updates
    @Bean
    public Queue stockUpdateQueue() {
        return new Queue(stockUpdateQueueName);
    }
    
    // Binding between exchange and queue
    @Bean
    public Binding stockUpdateBinding(Queue stockUpdateQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(stockUpdateQueue)
                .to(exchange)
                .with(stockUpdateRoutingKey);
    }
    
    // JSON message converter
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // RabbitTemplate with JSON conversion
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
} 