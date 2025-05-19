package com.example.AdminService.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.AdminService.dto.ProductUpdateDTO;
import com.example.AdminService.model.Product;
import com.example.AdminService.observer.ProductStockEventListener;
import com.example.AdminService.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private List<ProductStockEventListener> listeners = new ArrayList<>();
    private static final int DEFAULT_STOCK_THRESHOLD = 10;

    @Autowired
    public void init(EmailService emailService) {
        addEventListener(emailService);
    }

    public void addEventListener(ProductStockEventListener listener) {
        listeners.add(listener);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        if (product.getStockThreshold() == 0) {
            product.setStockThreshold(DEFAULT_STOCK_THRESHOLD);
        }
        Product savedProduct = productRepository.save(product);
        checkAndNotifyStockLevel(savedProduct);
        return savedProduct;
    }

    public Optional<Product> updateProduct(UUID id, ProductUpdateDTO updateDTO) {
        return productRepository.findById(id).map(existingProduct -> {
            // Only update fields that are present in the DTO
            if (updateDTO.getName() != null) {
                existingProduct.setName(updateDTO.getName());
            }
            if (updateDTO.getPrice() != null) {
                existingProduct.setPrice(updateDTO.getPrice());
            }
            if (updateDTO.getQuantity() != null) {
                existingProduct.setQuantity(updateDTO.getQuantity());
                checkAndNotifyStockLevel(existingProduct);
            }
            if (updateDTO.getDescription() != null) {
                existingProduct.setDescription(updateDTO.getDescription());
            }
            if (updateDTO.getStockThreshold() != null) {
                existingProduct.setStockThreshold(updateDTO.getStockThreshold());
                checkAndNotifyStockLevel(existingProduct);
            }
            
            return productRepository.save(existingProduct);
        });
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    private void checkAndNotifyStockLevel(Product product) {
        if (product.getQuantity() <= product.getStockThreshold()) {
            notifyLowStock(product);
        }
    }
    
    public Optional<Product> decreaseProductQuantity(UUID id, int amount) {
        return productRepository.findById(id).map(product -> {
            int newQuantity = product.getQuantity() - amount;
            if (newQuantity < 0) {
                System.out.println("Throwing exception due to insufficient stock");
                throw new IllegalArgumentException("Not enough stock available.");
            }

            product.setQuantity(newQuantity);
            checkAndNotifyStockLevel(product);
            return productRepository.save(product);
        });
    }
    
    /**
     * Increase stock for a product (for refunds)
     * 
     * @param id The product ID
     * @param amount The quantity to increase
     * @return The updated product
     */
    public Optional<Product> increaseProductQuantity(UUID id, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to increase must be positive");
        }
        
        return productRepository.findById(id).map(product -> {
            product.setQuantity(product.getQuantity() + amount);
            return productRepository.save(product);
        });
    }
    
    /**
     * Decrease stock for a product with the given string ID
     * 
     * @param productId The product ID as a string
     * @param quantity The quantity to decrease
     * @return The updated product
     * @throws IllegalArgumentException If the product is not found or has insufficient stock
     */
    public Product decreaseStock(String productId, int quantity) {
        try {
            UUID id = UUID.fromString(productId);
            return decreaseProductQuantity(id, quantity)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Product not found")) {
                throw e;
            }
            throw new IllegalArgumentException("Invalid product ID format: " + productId);
        }
    }
    
    /**
     * Increase stock for a product with the given string ID (for refunds)
     * 
     * @param productId The product ID as a string
     * @param quantity The quantity to increase
     * @return The updated product
     * @throws IllegalArgumentException If the product is not found
     */
    public Product increaseStock(String productId, int quantity) {
        try {
            UUID id = UUID.fromString(productId);
            return increaseProductQuantity(id, quantity)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Product not found")) {
                throw e;
            }
            throw new IllegalArgumentException("Invalid product ID format: " + productId);
        }
    }

    private void notifyLowStock(Product product) {
        for (ProductStockEventListener listener : listeners) {
            listener.onLowStock(product, product.getQuantity(), product.getStockThreshold());
        }
    }
}
