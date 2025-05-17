package com.example.AdminService.strategy;

import com.example.AdminService.model.Product;

public interface PromotionStrategy {
    double applyPromotion(double originalPrice, Double percentage, Double amount);
}