package com.example.AdminService.service;

import com.example.AdminService.model.Promotion;
import com.example.AdminService.model.PromotionType;
import com.example.AdminService.repository.PromotionRepository;
import com.example.AdminService.strategy.CartDiscountStrategy;
import com.example.AdminService.strategy.DiscountStrategy;
import com.example.AdminService.strategy.ItemDiscountStrategy;
import com.example.AdminService.strategy.FixedAmountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public Promotion createPromotion(Promotion promotion) {
        // 1. Check for duplicates first
        if (promotionRepository.existsByName(promotion.getName())) {
            throw new IllegalArgumentException(
                "Promotion name '" + promotion.getName() + "' already created"
            );
        }
        
        promotion.setId(UUID.randomUUID());

        if (promotion.getType() == PromotionType.CART_PROMOCODE &&
                promotion.getApplicableProductIds() != null &&
                !promotion.getApplicableProductIds().isEmpty()) {
            throw new IllegalArgumentException("CART_PROMOCODE promotions should not have applicable product IDs.");
        }

        return promotionRepository.save(promotion);
    }


    public Object getPromotionById(UUID id) {
        return promotionRepository.findById(id)
                .<Object>map(promotion -> promotion)
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Promotion with ID " + id + " not found");
                    return response;
                });
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Optional<Promotion> updatePromotion(UUID id, Promotion updatedPromotion) {
        return promotionRepository.findById(id).map(existing -> {
            updatedPromotion.setId(existing.getId());
            return promotionRepository.save(updatedPromotion);
        });
    }

    public Map<String, String> deletePromotion(UUID id) {
        if (promotionRepository.existsById(id)) {
            promotionRepository.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Promotion with ID " + id + " was deleted successfully.");
            return response;
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Promotion with ID " + id + " not found.");
            return response;
        }
    }


    public ResponseEntity<String> applyPromo(String promoCode, Map<UUID, Double> products) {
        Optional<Promotion> promoOpt = promotionRepository.findByName(promoCode);

        if (promoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Promo code not found");
        }

        Promotion p = promoOpt.get();

        if (!p.isActive()) {
            return ResponseEntity.badRequest().body("Promotion is not active");
        }

        if (p.getStartDate().isAfter(LocalDate.now()) || p.getEndDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body("Promotion is outside valid date range");
        }

        if (p.getType() == PromotionType.CART_PROMOCODE) {
            double total = products.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total <= 0) {
                return ResponseEntity.badRequest().body("Product list is empty. Cannot apply cart promotion.");
            }
            DiscountStrategy strategy = new CartDiscountStrategy(p.getDiscountValue());
            double discounted = strategy.applyDiscount(total);
            return ResponseEntity.ok("Cart promo applied successfully. New total: " + discounted);

        } else if (p.getType() == PromotionType.ITEM_DISCOUNT) {
            Map<UUID, Double> discountedItems = new HashMap<>();
            double newTotal = 0;

            for (Map.Entry<UUID, Double> entry : products.entrySet()) {
                UUID productId = entry.getKey();
                double price = entry.getValue();

                if (p.getApplicableProductIds().contains(productId)) {
                    DiscountStrategy strategy = new ItemDiscountStrategy(p.getDiscountValue());
                    double discounted = strategy.applyDiscount(price);
                    discountedItems.put(productId, discounted);
                    newTotal += discounted;
                } else {
                    newTotal += price;
                }
            }

            if (discountedItems.isEmpty()) {
                return ResponseEntity.badRequest().body("No products in list are eligible for this promo code");
            }

            String appliedTo = discountedItems.keySet().stream().map(UUID::toString).collect(Collectors.joining(", "));
            return ResponseEntity.ok("Item promo applied to products: [" + appliedTo + "]. New total: " + newTotal);
        }
        else if(p.getType() == PromotionType.FIXED_AMOUNT) {
            double total = products.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total <= 0) {
                return ResponseEntity.badRequest().body("Product list is empty. Cannot apply fixed amount promotion.");
            }
            DiscountStrategy strategy = new FixedAmountStrategy(p.getDiscountValue());
            double discounted = strategy.applyDiscount(total);
            return ResponseEntity.ok("Fixed amount promo applied successfully. New total: " + discounted);
        }
        return ResponseEntity.badRequest().body("Unsupported promotion type");
    }
}