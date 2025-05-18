package com.example.AdminService.observer;

import com.example.AdminService.model.Product;

public interface ProductStockEventListener {
    void onLowStock(Product product, int currentStock, int threshold);
} 