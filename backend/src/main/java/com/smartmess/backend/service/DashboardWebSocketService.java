package com.smartmess.backend.service;

import com.smartmess.backend.enums.MealSession;

public interface DashboardWebSocketService {
	
	void broadcastDashboard(MealSession mealSession);

}
