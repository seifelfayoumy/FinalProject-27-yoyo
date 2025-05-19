# Stock Update Consumer for AdminService

To implement the RabbitMQ consumer in the AdminService, follow these steps:

## 1. Add RabbitMQ dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

## 2. Create StockUpdateMessage Class

```java
package com.example.AdminService.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private int quantity;
    private String transactionId;
}
```

## 3. Configure RabbitMQ in application.properties

```properties
# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# RabbitMQ Queue name - must match the name in TransactionService
rabbitmq.queue.stock-update=stock_update_queue
```

## 4. Create RabbitMQ Configuration

```java
package com.example.AdminService.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
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
```

## 5. Implement the Consumer/Listener

```java
package com.example.AdminService.messaging;

import com.example.AdminService.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockUpdateConsumer {

    private final ProductService productService;
    
    @RabbitListener(queues = "${rabbitmq.queue.stock-update}")
    public void handleStockUpdate(StockUpdateMessage message) {
        try {
            log.info("Received stock update message: {}", message);
            
            // Call service to update product stock
            productService.decreaseStock(message.getProductId(), message.getQuantity());
            
            log.info("Successfully decreased stock for product: {}", message.getProductId());
        } catch (Exception e) {
            log.error("Error processing stock update message: {}", e.getMessage(), e);
            // Consider implementing a dead-letter queue for failed messages
        }
    }
}
```

## 6. Update ProductService to Include decreaseStock Method

```java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    
    // ... existing methods
    
    /**
     * Decrease product stock by the specified quantity
     * 
     * @param productId The ID of the product
     * @param quantity The quantity to decrease
     * @return The updated product
     * @throws IllegalArgumentException If product not found or insufficient stock
     */
    public Product decreaseStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        return productRepository.save(product);
    }
}
```

## 7. Enable RabbitMQ in AdminService Application

Add the @EnableRabbit annotation to your main application class:

```java
@SpringBootApplication
@EnableRabbit
public class AdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
} 