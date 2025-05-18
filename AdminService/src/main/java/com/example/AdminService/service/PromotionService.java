package com.example.AdminService.service;

import com.example.AdminService.model.Promotion;
import com.example.AdminService.model.PromotionType;
import com.example.AdminService.strategy.CartDiscountStrategy;
import com.example.AdminService.strategy.DiscountStrategy;
import com.example.AdminService.strategy.ItemDiscountStrategy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class PromotionService {

    private final List<Promotion> promotions = new ArrayList<>();

    public PromotionService() {
        // Hardcoded ITEM_DISCOUNT promotion (applies to a specific product)
        promotions.add(new Promotion(
                UUID.randomUUID(),
                "ITEM10",
                PromotionType.ITEM_DISCOUNT,
                10.0,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                true,
                List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        ));

        // Hardcoded CART_PROMOCODE promotion (applies to the entire cart)
        promotions.add(new Promotion(
                UUID.randomUUID(),
                "RAMADAN20",
                PromotionType.CART_PROMOCODE,
                20.0,
                LocalDate.of(2024, 3, 10),
                LocalDate.of(2024, 4, 10),
                true,
                Collections.emptyList()
        ));
    }

    public double applyItemPromotion(UUID productId, double originalPrice) {
        return promotions.stream()
                .filter(p -> p.getType() == PromotionType.ITEM_DISCOUNT
                        && p.getApplicableProductIds().contains(productId)
                        && p.isActive()
                        && !p.getStartDate().isAfter(LocalDate.now())
                        && !p.getEndDate().isBefore(LocalDate.now()))
                .findFirst()
                .map(p -> {
                    DiscountStrategy strategy = new ItemDiscountStrategy(p.getDiscountValue());
                    return strategy.applyDiscount(originalPrice);
                })
                .orElse(originalPrice);
    }

    public double applyCartPromotion(String code, double cartTotal) {
        return promotions.stream()
                .filter(p -> p.getType() == PromotionType.CART_PROMOCODE
                        && p.getName().equalsIgnoreCase(code)
                        && p.isActive()
                        && !p.getStartDate().isAfter(LocalDate.now())
                        && !p.getEndDate().isBefore(LocalDate.now()))
                .findFirst()
                .map(p -> {
                    DiscountStrategy strategy = new CartDiscountStrategy(p.getDiscountValue());
                    return strategy.applyDiscount(cartTotal);
                })
                .orElse(cartTotal);
    }
}
