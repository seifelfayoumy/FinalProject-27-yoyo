package com.example.AdminService.strategy;

import com.example.AdminService.model.Product;

public class FixedAmountPromotionStrategy implements PromotionStrategy {
    @Override
    public double applyPromotion(double originalPrice, Double percentage, Double amount) {
        if (amount == null) return originalPrice;
        return Math.max(0, originalPrice - amount);
    }
}