package com.example.AdminService.service;

import com.example.AdminService.model.Product;
import com.example.AdminService.repository.ProductRepository;
import com.example.AdminService.observer.ProductStockEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<Product> updateProduct(UUID id, Product updatedProduct) {
        return productRepository.findById(id).map(existingProduct -> {
            if (updatedProduct.getName() != null) {
                existingProduct.setName(updatedProduct.getName());
            }
            if (updatedProduct.getDescription() != null) {
                existingProduct.setDescription(updatedProduct.getDescription());
            }
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setQuantity(updatedProduct.getQuantity());
            existingProduct.setStockThreshold(updatedProduct.getStockThreshold());
            
            checkAndNotifyStockLevel(existingProduct);
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

    private void notifyLowStock(Product product) {
        for (ProductStockEventListener listener : listeners) {
            listener.onLowStock(product, product.getQuantity(), product.getStockThreshold());
        }
    }
}
