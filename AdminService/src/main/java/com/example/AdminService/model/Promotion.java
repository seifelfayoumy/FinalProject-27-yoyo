package com.example.AdminService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Document(collection = "Promotions")
public class Promotion {

    @Id
    private UUID id;
    @Indexed(unique = true)
    private String name; // promo code (e.g. "RAMADAN20")
    private PromotionType type; // CART_PROMOCODE or ITEM_DISCOUNT
    private double discountValue; // percentage
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private List<UUID> applicableProductIds; // for ITEM_DISCOUNT only

    public Promotion() {
        this.id = UUID.randomUUID();
    }

    public Promotion(UUID id, String name, PromotionType type, double discountValue,
                     LocalDate startDate, LocalDate endDate, boolean active,
                     List<UUID> applicableProductIds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.applicableProductIds = applicableProductIds;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<UUID> getApplicableProductIds() {
        return applicableProductIds;
    }

    public void setApplicableProductIds(List<UUID> applicableProductIds) {
        this.applicableProductIds = applicableProductIds;
    }

}
