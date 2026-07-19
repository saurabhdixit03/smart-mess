package com.smartmess.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_pricing")
public class MealPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealPricingId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal halfMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fullMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extraRotiPrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MealPricing() {
    }

    public Long getMealPricingId() {
        return mealPricingId;
    }

    public void setMealPricingId(Long mealPricingId) {
        this.mealPricingId = mealPricingId;
    }

    public BigDecimal getHalfMealPrice() {
        return halfMealPrice;
    }

    public void setHalfMealPrice(BigDecimal halfMealPrice) {
        this.halfMealPrice = halfMealPrice;
    }

    public BigDecimal getFullMealPrice() {
        return fullMealPrice;
    }

    public void setFullMealPrice(BigDecimal fullMealPrice) {
        this.fullMealPrice = fullMealPrice;
    }

    public BigDecimal getExtraRotiPrice() {
        return extraRotiPrice;
    }

    public void setExtraRotiPrice(BigDecimal extraRotiPrice) {
        this.extraRotiPrice = extraRotiPrice;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}