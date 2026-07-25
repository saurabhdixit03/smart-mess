package com.smartmess.backend.dto.response;

import com.smartmess.backend.enums.MealOption;
import java.time.LocalDateTime;
import com.smartmess.backend.enums.MealResponseStatus;

public class DashboardCustomerResponse {

    private Long customerId;

    private Long mealResponseId;

    private String customerName;

    private MealResponseStatus responseStatus;

    private MealOption mealOption;

    private Integer extraRotiCount;
    
    private LocalDateTime respondedAt;

    private Boolean collected;

    public DashboardCustomerResponse() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getMealResponseId() {
        return mealResponseId;
    }

    public void setMealResponseId(Long mealResponseId) {
        this.mealResponseId = mealResponseId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }


}