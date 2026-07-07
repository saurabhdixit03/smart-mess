package com.smartmess.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "meal_responses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"customer_id", "menu_id"})
    }
)
public class MealResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mealResponseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealResponseStatus responseStatus;

    @Enumerated(EnumType.STRING)
    private MealOption mealOption;

    @Column(nullable = false)
    private Integer extraRotiCount = 0;

    @Column(nullable = false)
    private LocalDateTime respondedAt;



    public MealResponse() {
    }

    public Long getMealResponseId() {
        return mealResponseId;
    }

    public void setMealResponseId(Long mealResponseId) {
        this.mealResponseId = mealResponseId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public MealResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(MealResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public MealOption getMealOption() {
        return mealOption;
    }

    public void setMealOption(MealOption mealOption) {
        this.mealOption = mealOption;
    }

    public Integer getExtraRotiCount() {
        return extraRotiCount;
    }

    public void setExtraRotiCount(Integer extraRotiCount) {
        this.extraRotiCount = extraRotiCount;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }


}