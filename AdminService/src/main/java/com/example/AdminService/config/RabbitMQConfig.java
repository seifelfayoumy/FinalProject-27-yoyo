package com.example.AdminService.config;

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
    
    @Value("${rabbitmq.queue.stock-update}")
    private String stockUpdateQueueName;
    
    @Value("${rabbitmq.queue.refund}")
    private String refundQueueName;
    
    @Value("${rabbitmq.exchange.name:stock-exchange}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing.key.stock-update:stock.update}")
    private String stockUpdateRoutingKey;
    
    @Value("${rabbitmq.routing.key.refund:refund.update}")
    private String refundRoutingKey;
    
    // Create stock update queue
    @Bean
    public Queue stockUpdateQueue() {
        return new Queue(stockUpdateQueueName, true);
    }
    
    // Create refund queue
    @Bean
    public Queue refundQueue() {
        return new Queue(refundQueueName, true);
    }
    
    // Create exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }
    
    // Bind stock update queue to exchange
    @Bean
    public Binding stockUpdateBinding(Queue stockUpdateQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(stockUpdateQueue)
                .to(exchange)
                .with(stockUpdateRoutingKey);
    }
    
    // Bind refund queue to exchange
    @Bean
    public Binding refundBinding(Queue refundQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(refundQueue)
                .to(exchange)
                .with(refundRoutingKey);
    }
    
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
} 