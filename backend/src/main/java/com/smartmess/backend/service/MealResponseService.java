package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.MealResponseResponse;

public interface MealResponseService {

    MealResponseResponse submitMealResponse(
            Long customerId,
            SubmitMealResponseRequest request);

    List<MealResponseResponse> getResponsesByMenu(
            Long menuId);

    MealResponseResponse getCustomerResponse(
            Long customerId,
            Long menuId);
}