package com.example.TransactionService.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Long id;
    private String email;
    private String fullName;
    private Boolean isEmailVerified;
} 