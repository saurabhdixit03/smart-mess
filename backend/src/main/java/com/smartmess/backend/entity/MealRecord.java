package com.smartmess.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MealOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "meal_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    /*
     * Optional.
     * Null when the owner manually creates a meal record
     * for a customer who did not submit a meal response.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_response_id")
    private MealResponse mealResponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealOption mealOption;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mealPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer extraRotiCount = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal extraRotiPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

}