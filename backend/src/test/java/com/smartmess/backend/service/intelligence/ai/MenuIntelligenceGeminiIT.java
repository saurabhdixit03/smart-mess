
package com.smartmess.backend.service.intelligence.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.smartmess.backend.repository.MenuIntelligenceAnalysisRunRepository;
import com.smartmess.backend.repository.MenuIntelligenceSuggestionRepository;
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

                // Disable scheduled analysis during this integration test.
                "app.menu-intelligence.scheduler.cron=0 0 0 31 2 *"
        }
)
@ContextConfiguration(
        initializers = MenuIntelligenceGeminiIT.DatabaseGuard.class
)
class MenuIntelligenceGeminiIT {

    private static final Long MESS_ID = 1L;

    private static final LocalDate CURRENT_PERIOD_START =
            LocalDate.of(2026, 10, 28);

    private static final LocalDate CURRENT_PERIOD_END =
            LocalDate.of(2026, 11, 3);

    private static final LocalDate PREVIOUS_PERIOD_START =
            LocalDate.of(2026, 10, 21);

    private static final LocalDate PREVIOUS_PERIOD_END =
            LocalDate.of(2026, 10, 27);

    private static final String FIND_RUN_SQL = """
            SELECT analysis_run_id
            FROM menu_intelligence_analysis_runs
            WHERE mess_id = ?
              AND period_start = ?
              AND period_end = ?
            """;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private MenuIntelligenceAnalysisService analysisService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MenuIntelligenceAnalysisRunRepository analysisRunRepository;

    @Autowired
    private MenuIntelligenceSuggestionRepository suggestionRepository;

    private boolean cleanupEligible;

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

        // Refuse to reuse or overwrite an existing analysis run.
        List<Long> existingRunIds = findRunIds();

        assertTrue(
                existingRunIds.isEmpty(),
                "A run already exists for the test period. "
                        + "Refusing to reuse or modify existing data."
        );

        // Cleanup is permitted only after confirming the period was unused.
        cleanupEligible = true;

        boolean successful = analysisService.analyzeMess(
                MESS_ID,
                CURRENT_PERIOD_START,
                CURRENT_PERIOD_END,
                PREVIOUS_PERIOD_START,
                PREVIOUS_PERIOD_END
        );

        List<Long> runIds = findRunIds();

        assertEquals(
                1,
                runIds.size(),
                "Expected exactly one analysis run for the test period."
        );

        Long analysisRunId = runIds.get(0);

        assertTrue(
                successful,
                "Gemini menu intelligence analysis failed. "
                        + "Check backend logs for the actual exception."
        );

        Map<String, Object> run = jdbcTemplate.queryForMap(
                """
                SELECT status, suggestion_count
                FROM menu_intelligence_analysis_runs
                WHERE analysis_run_id = ?
                """,
                analysisRunId
        );

        assertEquals(
                "SUCCESS",
                run.get("status").toString(),
                "Analysis run should be marked SUCCESS."
        );

        Integer persistedSuggestionCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM menu_intelligence_suggestions
                WHERE analysis_run_id = ?
                """,
                Integer.class,
                analysisRunId
        );

        assertNotNull(persistedSuggestionCount);

        int reportedSuggestionCount =
                ((Number) run.get("suggestion_count")).intValue();

        assertTrue(
                persistedSuggestionCount > 0,
                "Expected Gemini suggestions to be persisted."
        );

        assertEquals(
                reportedSuggestionCount,
                persistedSuggestionCount,
                "Persisted suggestion count should match the analysis run."
        );
    }

    private List<Long> findRunIds() {

        return jdbcTemplate.queryForList(
                FIND_RUN_SQL,
                Long.class,
                MESS_ID,
                CURRENT_PERIOD_START,
                CURRENT_PERIOD_END
        );
    }

    @AfterEach
    void cleanupTestRun() {

        if (!cleanupEligible) {
            return;
        }

        List<Long> runIds = findRunIds();

        if (runIds.isEmpty()) {
            return;
        }

        if (runIds.size() != 1) {
            throw new IllegalStateException(
                    "Unexpected number of runs for the test period. "
                            + "Cleanup was skipped."
            );
        }

        Long analysisRunId = runIds.get(0);

        TransactionTemplate cleanupTransaction =
                new TransactionTemplate(transactionManager);

        cleanupTransaction.executeWithoutResult(status -> {

            suggestionRepository
                    .deleteAllByAnalysisRun_AnalysisRunId(analysisRunId);

            analysisRunRepository.deleteById(analysisRunId);
        });
    }
}
