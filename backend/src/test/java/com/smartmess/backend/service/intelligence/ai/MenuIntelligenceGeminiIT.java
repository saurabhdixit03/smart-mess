
package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.smartmess.backend.service.EmailService;

@SpringBootTest(
        properties = {
                "spring.ai.model.chat=google-genai",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.show-sql=false",

                "app.seed-demo-data=false",
                "app.billing.automation.enabled=false",
                "app.mail.notifications.enabled=false",
                "app.cashfree.enabled=false",

                "app.menu-intelligence.debug-raw-response=true",

                // February 31 never occurs.
                "app.menu-intelligence.scheduler.cron=0 0 0 31 2 *"
        }
)
@ContextConfiguration(
        initializers = MenuIntelligenceGeminiIT.DatabaseGuard.class
)
class MenuIntelligenceGeminiIT {

    private static final Long MESS_ID = 1L;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MenuIntelligenceAnalysisService analysisService;

    static class DatabaseGuard
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context) {

            String url = context.getEnvironment()
                    .getProperty("spring.datasource.url", "");

            if (!"jdbc:mysql://localhost:3306/smart_mess".equals(url)) {
                throw new IllegalStateException(
                        "Gemini integration test requires the exact "
                                + "localhost smart_mess database."
                );
            }
        }
    }

    @Test
    void generateAndPersistGeminiSuggestionsForDemoMess() {

        // Generate a unique seven-day period for this execution.
        // This prevents conflicts with previously successful analysis runs.
        long uniqueOffset = Math.floorMod(System.nanoTime(), 1_000_000L);

        LocalDate currentPeriodStart =
                LocalDate.of(2000, 1, 1).plusDays(uniqueOffset);

        LocalDate currentPeriodEnd = currentPeriodStart.plusDays(6);

        LocalDate previousPeriodEnd = currentPeriodStart.minusDays(1);

        LocalDate previousPeriodStart = previousPeriodEnd.minusDays(6);

        boolean successful = analysisService.analyzeMess(
                MESS_ID,
                currentPeriodStart,
                currentPeriodEnd,
                previousPeriodStart,
                previousPeriodEnd
        );

        assertTrue(
                successful,
                "Menu intelligence analysis failed. "
                        + "Check backend logs for the actual exception."
        );
    }
}
