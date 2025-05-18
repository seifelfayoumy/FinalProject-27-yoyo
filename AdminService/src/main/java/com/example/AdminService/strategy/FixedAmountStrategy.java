package com.example.AdminService.strategy;

public class FixedAmountStrategy implements DiscountStrategy {
    private final double fixedDiscount;

    public FixedAmountStrategy(double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    @Override
    public double applyDiscount(double amount) {
        double discounted = amount - fixedDiscount;
        return discounted > 0 ? discounted : 0;
    }
}