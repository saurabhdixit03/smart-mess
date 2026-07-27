package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.CollectionQueueResponse;
import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.enums.MealSession;

public interface MealRecordService {
	MealRecordResponse createMealRecord(
	        CreateMealRecordRequest request);

	List<MealRecordResponse> getCustomerMealHistory(
	        Long customerId);

	List<MealRecordResponse> getTodayMealRecords(
	        MealSession mealSession);
	
	// Collection queue for meal record module 
	
	List<CollectionQueueResponse> getCollectionQueue(
	        MealSession mealSession);
}
