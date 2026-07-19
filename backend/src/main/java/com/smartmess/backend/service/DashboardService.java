package com.smartmess.backend.service;

import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.enums.MealSession;

public interface DashboardService {

	DashboardSummaryResponse getDashboardSummary(MealSession mealSession);

}