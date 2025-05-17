package com.example.AdminService.service;

import com.example.AdminService.model.Product;
import com.example.AdminService.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(UUID id, Product updatedProduct) {
        return productRepository.findById(id).map(existing -> {
            existing.setName(updatedProduct.getName());
            existing.setPrice(updatedProduct.getPrice());
            existing.setQuantity(updatedProduct.getQuantity());
            existing.setDescription(updatedProduct.getDescription());
            return productRepository.save(existing);
        });
    }

    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }
}
