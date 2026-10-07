package com.smartmess.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "meal_pricing",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_meal_pricing_mess_effective",
                        columnNames = {
                                "mess_id",
                                "effective_from"
                        }
                )
        }
)
public class MealPricing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealPricingId;

    /*
     * Each mess can have multiple pricing versions.
     * Existing collected meals retain their stored prices.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal halfMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fullMealPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extraRotiPrice;

    /*
     * Initial pricing uses a baseline date.
     * Scheduled pricing will receive an explicit effective time
     * from the service using the application's configured clock.
     *
     * This default preserves existing initialization callers.
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom =
            LocalDateTime.of(1970, 1, 1, 0, 0);

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

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
}