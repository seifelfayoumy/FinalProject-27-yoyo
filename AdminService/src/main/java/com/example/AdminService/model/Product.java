package com.example.AdminService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "Products")
public class Product {
    @Id
    private UUID id;
    private String name;
    private double price;
    private int quantity;
    private String description;
    private int stockThreshold;

    // Constructors
    public Product() {
        this.id = UUID.randomUUID();
    }

    // Constructors without threshold
    public Product(UUID id, String name, double price, int quantity, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    public Product(String name, double price, int quantity, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
    }

    // Constructors with threshold
    public Product(UUID id, String name, double price, int quantity, String description, int stockThreshold) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.stockThreshold = stockThreshold;
    }

    public Product(String name, double price, int quantity, String description, int stockThreshold) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.stockThreshold = stockThreshold;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStockThreshold() { return stockThreshold; }
    public void setStockThreshold(int stockThreshold) { this.stockThreshold = stockThreshold; }
}
