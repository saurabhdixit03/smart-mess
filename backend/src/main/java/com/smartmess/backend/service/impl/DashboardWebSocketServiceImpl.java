package com.smartmess.backend.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.service.DashboardService;
import com.smartmess.backend.service.DashboardWebSocketService;

@Service
public class DashboardWebSocketServiceImpl
        implements DashboardWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DashboardService dashboardService;

    public DashboardWebSocketServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            DashboardService dashboardService) {

        this.messagingTemplate = messagingTemplate;
        this.dashboardService = dashboardService;
    }

    @Override
    public void broadcastDashboard(MealSession mealSession) {

        DashboardSummaryResponse dashboard =
                dashboardService.getDashboardSummary(mealSession);

        messagingTemplate.convertAndSend(
                "/topic/dashboard/" + mealSession.name(),
                dashboard
        );
    }

}