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

    // Constructors
    public Product() {
        this.id = UUID.randomUUID(); // Auto-generate ID if not provided
    }

    public Product(UUID id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;

    }

    public Product(String name, double price, int quantity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    //Getters and Setters


}
