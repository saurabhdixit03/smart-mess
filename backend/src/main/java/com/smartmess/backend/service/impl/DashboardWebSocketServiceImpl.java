package com.smartmess.backend.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DashboardService;
import com.smartmess.backend.service.DashboardWebSocketService;

@Service
public class DashboardWebSocketServiceImpl
        implements DashboardWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DashboardService dashboardService;
    private final CustomerSecurity customerSecurity;

    public DashboardWebSocketServiceImpl(
            SimpMessagingTemplate messagingTemplate,
            DashboardService dashboardService,
            CustomerSecurity customerSecurity) {

        this.messagingTemplate = messagingTemplate;
        this.dashboardService = dashboardService;
        this.customerSecurity = customerSecurity;
    }

    @Override
    public void broadcastDashboard(
            MealSession mealSession) {

        Long messId =
                customerSecurity.getCurrentMessId();

        DashboardSummaryResponse dashboard =
                dashboardService.getDashboardSummary(mealSession);

        /*
         * Each mess has separate dashboard destinations.
         *
         * Subscription authorisation must verify both
         * the owner's role and this mess ID.
         */
        messagingTemplate.convertAndSend(
                "/topic/dashboard/"
                        + messId
                        + "/"
                        + mealSession.name(),
                dashboard
        );
    }
}