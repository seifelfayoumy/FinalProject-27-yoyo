package com.example.AdminService.dto;

public class ProductUpdateDTO {
    private String name;
    private Double price;
    private Integer quantity;
    private String description;
    private Integer stockThreshold;
    private String category;

    // Using wrapper classes (Double, Integer) instead of primitives (double, int)
    // so we can check if a field was included in the request

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStockThreshold() {
        return stockThreshold;
    }

    public void setStockThreshold(Integer stockThreshold) {
        this.stockThreshold = stockThreshold;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
} 