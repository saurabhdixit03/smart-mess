package com.smartmess.backend.dto.request;

import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitMealResponseRequest {

    // Temporary until authentication is implemented
    @NotNull(message = "Customer ID is required.")
    private Long customerId;

    @NotNull(message = "Menu ID is required.")
    private Long menuId;

    @NotNull(message = "Response status is required.")
    private MealResponseStatus responseStatus;

    private MealOption mealOption;

    @NotNull(message = "Extra roti count is required.")
    @Min(0)
    @Max(5)
    private Integer extraRotiCount;

    public SubmitMealResponseRequest() {
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
}