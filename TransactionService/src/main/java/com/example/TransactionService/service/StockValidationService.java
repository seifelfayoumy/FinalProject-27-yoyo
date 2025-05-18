package com.example.TransactionService.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.TransactionService.client.AdminServiceClient;
import com.example.TransactionService.client.ProductDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockValidationService {

    private final AdminServiceClient adminServiceClient;
    
    /**
     * Validates if there's sufficient stock for the products in the format "productId quantity".
     * 
     * @param products List of products in the format "productId quantity"
     * @return List of product names with insufficient stock, empty if all products have sufficient stock
     */
    public List<String> validateStock(List<String> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> insufficientStockProducts = new ArrayList<>();
        
        for (String productEntry : products) {
            String[] parts = productEntry.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid product format: " + productEntry + ". Expected format: productId quantity");
            }
            
            try {
                String productId = parts[0];
                Integer requestedQuantity = Integer.parseInt(parts[1]);
                
                ProductDTO product = adminServiceClient.getProductById(productId);
                
                if (product == null) {
                    insufficientStockProducts.add("Product ID " + productId + " not found");
                } else if (product.getQuantity() < requestedQuantity) {
                    insufficientStockProducts.add(product.getName() + " (only " + product.getQuantity() + " available)");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid quantity format in: " + productEntry);
            } catch (Exception e) {
                // Handle case where the admin service might be down or product doesn't exist
                insufficientStockProducts.add("Unable to validate product ID " + parts[0] + ": " + e.getMessage());
            }
        }
        
        return insufficientStockProducts;
    }
} 