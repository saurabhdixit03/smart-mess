package com.smartmess.backend.dto.response;

import java.time.LocalDate;

import com.smartmess.backend.enums.MealSession;
import java.util.List;

public class DashboardSummaryResponse {
	
	private LocalDate menuDate;

	// Current Dashboard Session
    private MealSession mealSession;
	
    // Customer Statistics
    private Long activeCustomers;

    // Response Statistics
    private Long acceptedResponses;
    private Long declinedResponses;
    private Long pendingResponses;

    // Meal Estimation
    private Long expectedFullMeals;
    private Long expectedHalfMeals;
    private Long expectedExtraRotis;
    
    private Long totalRotisRequired;
    
    private List<DashboardCustomerResponse> collectionQueue;

    
    
    public DashboardSummaryResponse() {
    }
    
    public MealSession getMealSession() {
        return mealSession;
    }

    public void setMealSession(MealSession mealSession) {
        this.mealSession = mealSession;
    }

    public Long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public Long getAcceptedResponses() {
        return acceptedResponses;
    }

    public void setAcceptedResponses(Long acceptedResponses) {
        this.acceptedResponses = acceptedResponses;
    }

    public Long getDeclinedResponses() {
        return declinedResponses;
    }

    public void setDeclinedResponses(Long declinedResponses) {
        this.declinedResponses = declinedResponses;
    }

    public Long getPendingResponses() {
        return pendingResponses;
    }

    public void setPendingResponses(Long pendingResponses) {
        this.pendingResponses = pendingResponses;
    }

    public Long getExpectedFullMeals() {
        return expectedFullMeals;
    }

    public void setExpectedFullMeals(Long expectedFullMeals) {
        this.expectedFullMeals = expectedFullMeals;
    }

    public Long getExpectedHalfMeals() {
        return expectedHalfMeals;
    }

    public void setExpectedHalfMeals(Long expectedHalfMeals) {
        this.expectedHalfMeals = expectedHalfMeals;
    }

    public Long getExpectedExtraRotis() {
        return expectedExtraRotis;
    }

    public void setExpectedExtraRotis(Long expectedExtraRotis) {
        this.expectedExtraRotis = expectedExtraRotis;
    }

	public LocalDate getMenuDate() {
		return menuDate;
	}

	public void setMenuDate(LocalDate menuDate) {
		this.menuDate = menuDate;
	}

	public Long getTotalRotisRequired() {
		return totalRotisRequired;
	}

	public void setTotalRotisRequired(Long totalRotisRequired) {
		this.totalRotisRequired = totalRotisRequired;
	}

	public List<DashboardCustomerResponse> getCollectionQueue() {
		return collectionQueue;
	}

	public void setCollectionQueue(List<DashboardCustomerResponse> collectionQueue) {
		this.collectionQueue = collectionQueue;
	}
}