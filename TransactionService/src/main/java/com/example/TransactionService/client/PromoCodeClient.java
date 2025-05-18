package com.example.TransactionService.client;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "admin-service", url = "http://localhost:8084/api/promotions")
public interface PromoCodeClient {

    @PostMapping("/apply")
    String applyPromo(@RequestParam("promoCode") String promoCode,
                      @RequestBody Map<UUID, Double> products);
}
