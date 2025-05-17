package com.example.AdminService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "Promotions")
public class Promotion {
    @Id
    private UUID id;
    private String name;
    private PromotionType type;
    private Double amount; 
    private boolean active;

    public Promotion(UUID id, String name, PromotionType type, double amount, boolean active) {
        this.id            = id;
        this.name          = name;
        this.type          = type;
        this.amount        = amount;
        this.active        = active;
    }
    public Promotion(String name, PromotionType type, double amount, boolean active) {
        this.id            = UUID.randomUUID();
        this.name          = name;
        this.type          = type;
        this.amount        = amount;
        this.active        = active;
    }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
