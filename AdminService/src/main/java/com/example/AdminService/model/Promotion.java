package com.example.AdminService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "Promotions")
public class Promotion {
    @Id
    private UUID id;

    private String name;

    /**
     * ENUM: e.g. PERCENTAGE, FIXED_AMOUNT, BOGO, FLASH_SALE
     */
    private PromotionType type;

}
