
package com.smartmess.backend.service.intelligence.ai;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.smartmess.backend.security.CustomerSecurity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuIntelligenceTriggerService {

    private static final ZoneId ANALYSIS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final CustomerSecurity customerSecurity;

    private final ObjectProvider<MenuIntelligenceAnalysisService>
            analysisServiceProvider;

    public boolean triggerAnalysis() {

        MenuIntelligenceAnalysisService analysisService =
                analysisServiceProvider.getIfAvailable();

        if (analysisService == null) {
            throw new IllegalStateException(
                    "AI menu analysis is currently unavailable."
            );
        }

        // Resolve tenant from the authenticated account.
        Long messId = customerSecurity.getCurrentMessId();

        // Use the same weekly periods as the existing scheduler.
        LocalDate currentPeriodEnd =
                LocalDate.now(ANALYSIS_ZONE).with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.SATURDAY
                        )
                );

        LocalDate currentPeriodStart =
                currentPeriodEnd.minusDays(6);

        LocalDate previousPeriodEnd =
                currentPeriodStart.minusDays(1);

        LocalDate previousPeriodStart =
                previousPeriodEnd.minusDays(6);

        return analysisService.analyzeMess(
                messId,
                currentPeriodStart,
                currentPeriodEnd,
                previousPeriodStart,
                previousPeriodEnd
        );
    }
}
