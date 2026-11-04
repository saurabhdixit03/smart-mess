package com.smartmess.backend.service.intelligence.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContextBuilder;
import com.smartmess.backend.service.intelligence.ai.MenuIntelligenceSuggestionPersistenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.ai.model.chat",
        havingValue = "google-genai"
)
public class MenuIntelligenceAnalysisService {

    private final MenuIntelligenceContextBuilder contextBuilder;
    private final MenuFeedbackAiProcessingService aiProcessingService;
    private final MenuIntelligenceSuggestionPersistenceService persistenceService;

    public boolean analyzeMess(
            Long messId,
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            LocalDate previousPeriodStart,
            LocalDate previousPeriodEnd
    ) {
        Long analysisRunId = null;

        try {
            // 1. Start and persist the analysis run.
            analysisRunId = persistenceService.startAnalysisRun(
                    messId,
                    currentPeriodStart,
                    currentPeriodEnd
            );

            // 2. Build historical intelligence with weekly comparison.
            MenuIntelligenceContext context = contextBuilder.buildWithComparison(
                    messId,
                    currentPeriodStart,
                    currentPeriodEnd,
                    previousPeriodStart,
                    previousPeriodEnd
            );

            // 3. Generate and validate AI suggestions.
            var suggestions = aiProcessingService.process(context);

            // 4. Persist suggestions and mark the run successful.
            persistenceService.completeAnalysisRun(
                    messId,
                    analysisRunId,
                    suggestions
            );

            log.info(
                    "Menu intelligence analysis completed for messId={}, analysisRunId={}",
                    messId,
                    analysisRunId
            );

            return true;

        } catch (RuntimeException exception) {

            // 5. Mark the run failed if it was successfully started.
            if (analysisRunId != null) {
                try {
                    persistenceService.markAnalysisRunFailed(
                            messId,
                            analysisRunId
                    );
                } catch (RuntimeException failureException) {
                    log.error(
                            "Could not mark analysis run as failed. messId={}, analysisRunId={}, failureType={}",
                            messId,
                            analysisRunId,
                            failureException.getClass().getSimpleName()
                    );
                }
            }

            log.error(
                    "Menu intelligence analysis failed. messId={}, analysisRunId={}, failureType={}, message={}",
                    messId,
                    analysisRunId,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );

            return false;
        }
    }
}