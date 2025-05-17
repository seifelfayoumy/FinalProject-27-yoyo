package com.example.AdminService.strategy;

import com.example.AdminService.model.Product;

public class PromotionContext {
    private PromotionStrategy strategy;
    public void setStrategy(PromotionStrategy strategy) {
        this.strategy = strategy;
    }
    public double applyPromotion(Product product, double originalPrice) {
        if (strategy == null) return originalPrice;
        return strategy.applyPromotion(product, originalPrice);
    }
}