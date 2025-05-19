package com.example.TransactionService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admin-service", url = "${admin.service.url}/api/promotions")
public interface PromoCodeClient {

    @PostMapping("/apply")
    String applyPromo(@RequestParam("promoCode") String promoCode,
                      @RequestParam("total") double total);
}
