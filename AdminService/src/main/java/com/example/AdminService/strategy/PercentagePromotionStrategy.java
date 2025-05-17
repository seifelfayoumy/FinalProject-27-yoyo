package com.example.AdminService.strategy;

import com.example.AdminService.model.Product;

public class PercentagePromotionStrategy implements PromotionStrategy {
    @Override
    public double applyPromotion( double originalPrice, Double percentage, Double amount) {
        if (percentage == null) return originalPrice;
        return originalPrice * (1 - percentage / 100.0);
    }
}