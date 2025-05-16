package com.example.TransactionService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url  = "${user.service.url}"
)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserModel getById(@PathVariable("id") Long id);

    @GetMapping("/api/users/validate-token")
    TokenValidationResponse validateToken(@RequestParam("token") String token);

    // add other calls as needed...
}