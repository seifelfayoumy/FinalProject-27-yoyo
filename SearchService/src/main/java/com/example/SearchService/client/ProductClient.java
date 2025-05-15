package com.example.SearchService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "admin-product")
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
     */
    class Product {
        private Long id;
        private String name;
        private String description;
        private String category;
        private Double price;
        private Integer quantity;
        
        // Default constructor
        public Product() {
        }
        
        // Constructor with all fields
        public Product(Long id, String name, String description, String category, Double price, Integer quantity) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
        }
        
        // Getters and setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        @Override
        public String toString() {
            return "Product{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", category='" + category + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
