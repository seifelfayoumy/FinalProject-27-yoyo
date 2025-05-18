package com.example.SearchService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "admin-product", url = "${admin-product.url}")
public interface ProductClient {
    
    /**
     * Fetches all products from Admin_Product service.
     *
     * @return List of all available products
     */
    @GetMapping("/api/products")
    List<Product> getAllProducts();
    
    /**
     * Product data class representing a product retrieved from Admin_Product service.
     * This class matches the structure of the AdminService Product model.
     */
    class Product {
        private String id; // Stores UUID as string
        private String name;
        private double price; // Changed to primitive double to match AdminService
        private int quantity; // Changed to primitive int to match AdminService
        private String description;
        private int stockThreshold; // Added missing field from AdminService
        private String category; // Added category field to match AdminService
        
        // Default constructor
        public Product() {
        }
        
        // Constructor with all fields
        public Product(String id, String name, double price, int quantity, String description, int stockThreshold, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.description = description;
            this.stockThreshold = stockThreshold;
            this.category = category;
        }
        
        // Getters and setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public int getStockThreshold() {
            return stockThreshold;
        }
        
        public void setStockThreshold(int stockThreshold) {
            this.stockThreshold = stockThreshold;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        @Override
        public String toString() {
            return "Product{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    ", description='" + description + '\'' +
                    ", stockThreshold=" + stockThreshold +
                    ", category='" + category + '\'' +
                    '}';
        }
    }
}
