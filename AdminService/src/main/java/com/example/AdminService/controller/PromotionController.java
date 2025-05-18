package com.example.AdminService.controller;

import com.example.AdminService.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/apply-item")
    public ResponseEntity<Double> applyItemDiscount(@RequestParam UUID productId, @RequestParam double price) {
        double discountedPrice = promotionService.applyItemPromotion(productId, price);
        return ResponseEntity.ok(discountedPrice);
    }

    @GetMapping("/apply-cart")
    public ResponseEntity<Double> applyCartDiscount(@RequestParam String promoCode, @RequestParam double total) {
        double discountedTotal = promotionService.applyCartPromotion(promoCode, total);
        return ResponseEntity.ok(discountedTotal);
    }
}
