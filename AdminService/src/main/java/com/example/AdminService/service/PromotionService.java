package com.example.AdminService.service;

import com.example.AdminService.model.Promotion;
import com.example.AdminService.model.PromotionType;
import com.example.AdminService.repository.PromotionRepository;
import com.example.AdminService.strategy.CartDiscountStrategy;
import com.example.AdminService.strategy.DiscountStrategy;
import com.example.AdminService.strategy.ItemDiscountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public Promotion createPromotion(Promotion promotion) {
        promotion.setId(UUID.randomUUID());
        return promotionRepository.save(promotion);
    }

    public Optional<Promotion> getPromotionById(UUID id) {
        return promotionRepository.findById(id);
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

    public void deletePromotion(UUID id) {
        promotionRepository.deleteById(id);
    }

    public double applyItemPromotion(UUID productId, double originalPrice) {
        return promotionRepository.findAll().stream()
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
        return promotionRepository.findByName(code)
                .filter(p -> p.getType() == PromotionType.CART_PROMOCODE
                        && p.isActive()
                        && !p.getStartDate().isAfter(LocalDate.now())
                        && !p.getEndDate().isBefore(LocalDate.now()))
                .map(p -> {
                    DiscountStrategy strategy = new CartDiscountStrategy(p.getDiscountValue());
                    return strategy.applyDiscount(cartTotal);
                })
                .orElse(cartTotal);
    }
}
