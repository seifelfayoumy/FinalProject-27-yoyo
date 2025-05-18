package com.example.TransactionService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "admin-service",
        url = "${admin.service.url}"
)
public interface AdminServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") String productId);
} 