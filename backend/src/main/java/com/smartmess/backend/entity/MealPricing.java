package com.smartmess.backend.entity;

import java.math.BigDecimal;

import com.smartmess.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_pricing")
public class MealPricing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealPricingId;

    /*
     * Tenant Ownership
     *
     * Each mess has its own current meal pricing record.
     * Existing prices are updated within that record.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false, unique = true)
    private Mess mess;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal halfMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fullMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extraRotiPrice;

    public MealPricing() {
    }

    public Long getMealPricingId() {
        return mealPricingId;
    }

    public void setMealPricingId(Long mealPricingId) {
        this.mealPricingId = mealPricingId;
    }

    public Mess getMess() {
        return mess;
    }

    public void setMess(Mess mess) {
        this.mess = mess;
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
}