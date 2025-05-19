package com.example.AdminService.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.AdminService.model.Promotion;
import com.example.AdminService.service.PromotionService;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody Promotion promotion) {
        try {
            Promotion saved = promotionService.createPromotion(promotion);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body("promoCode name already exists");
        }
    }

    @GetMapping
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Promotion> updatePromotion(@PathVariable UUID id, @RequestBody Promotion updatedPromotion) {
        Optional<Promotion> updated = promotionService.updatePromotion(id, updatedPromotion);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID id) {
        Map<String, String> result = promotionService.deletePromotion(id);

        if (result.get("message").contains("not found")) {
            return ResponseEntity.status(404).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }


    @PostMapping("/apply")
    public ResponseEntity<String> applyPromo(@RequestParam String promoCode, @RequestParam double total) {
        System.out.println("PROMO DEBUG: AdminService received request to apply promo: " + promoCode + " to total: " + total);
        ResponseEntity<String> result = promotionService.applyPromo(promoCode, total);
        System.out.println("PROMO DEBUG: AdminService response: " + result.getBody() + " with status: " + result.getStatusCode());
        return result;
    }
}