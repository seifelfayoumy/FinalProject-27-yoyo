package com.example.AdminService.strategy;

public class CartDiscountStrategy implements DiscountStrategy {

    private final double discountPercentage;

    public CartDiscountStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double applyDiscount(double amount) {
        return amount - (amount * discountPercentage / 100);
    }
}
