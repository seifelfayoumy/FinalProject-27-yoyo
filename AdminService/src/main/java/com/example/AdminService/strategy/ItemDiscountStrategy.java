package com.example.AdminService.strategy;


public class ItemDiscountStrategy implements DiscountStrategy {

    private final double discountPercentage;

    public ItemDiscountStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double applyDiscount(double amount) {
        return amount - (amount * discountPercentage / 100);
    }
}
