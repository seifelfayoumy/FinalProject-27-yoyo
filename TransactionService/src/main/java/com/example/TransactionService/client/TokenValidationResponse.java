package com.example.TransactionService.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    private boolean success;
    private Long userId;
    private String email;
} 