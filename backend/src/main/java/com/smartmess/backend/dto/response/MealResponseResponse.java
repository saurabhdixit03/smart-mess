package com.smartmess.backend.dto.response;

import java.time.LocalDateTime;

import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

public class MealResponseResponse {

    private Long mealResponseId;

    private Long customerId;

    private Long menuId;

    private MealResponseStatus responseStatus;

    private MealOption mealOption;

    private Integer extraRotiCount;

    private LocalDateTime respondedAt;

    public MealResponseResponse() {
    }

    public Long getMealResponseId() {
        return mealResponseId;
    }

    public void setMealResponseId(Long mealResponseId) {
        this.mealResponseId = mealResponseId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
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