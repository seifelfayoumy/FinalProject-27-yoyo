package com.example.TransactionService.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    
    @Value("${rabbitmq.queue.refund}")
    private String refundQueueName;
    
    @Value("${rabbitmq.routing.key.refund}")
    private String refundRoutingKey;
    
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
    
    // Queue for refunds
    @Bean
    public Queue refundQueue() {
        return new Queue(refundQueueName);
    }
    
    // Binding between exchange and stock update queue
    @Bean
    public Binding stockUpdateBinding(Queue stockUpdateQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(stockUpdateQueue)
                .to(exchange)
                .with(stockUpdateRoutingKey);
    }
    
    // Binding between exchange and refund queue
    @Bean
    public Binding refundBinding(Queue refundQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(refundQueue)
                .to(exchange)
                .with(refundRoutingKey);
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