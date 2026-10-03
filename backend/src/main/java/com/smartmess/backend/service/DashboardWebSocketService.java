package com.smartmess.backend.service;

import com.smartmess.backend.enums.MealSession;

public interface DashboardWebSocketService {

    /*
     * Broadcasts the requested session's dashboard
     * within the authenticated mess.
     */
    void broadcastDashboard(MealSession mealSession);
}