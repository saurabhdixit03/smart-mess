package com.smartmess.backend.service.intelligence.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.MenuIntelligenceAnalysisRun;
import com.smartmess.backend.entity.MenuIntelligenceSuggestion;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.AnalysisRunStatus;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MenuIntelligenceAnalysisRunRepository;
import com.smartmess.backend.repository.MenuIntelligenceSuggestionRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.intelligence.ai.dto.OverallMealSuggestion;

@Service
public class MenuIntelligenceSuggestionPersistenceService {

    private final MessRepository messRepository;
    private final DishRepository dishRepository;
    private final MenuIntelligenceAnalysisRunRepository analysisRunRepository;
    private final MenuIntelligenceSuggestionRepository suggestionRepository;

    public MenuIntelligenceSuggestionPersistenceService(
            MessRepository messRepository,
            DishRepository dishRepository,
            MenuIntelligenceAnalysisRunRepository analysisRunRepository,
            MenuIntelligenceSuggestionRepository suggestionRepository) {

        this.messRepository = messRepository;
        this.dishRepository = dishRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.suggestionRepository = suggestionRepository;
    }

    /**
     * Creates an IN_PROGRESS analysis run.
     * A previously successful or currently running period
     * cannot be processed again.
     */
    @Transactional
    public Long startAnalysisRun(
            Long messId,
            LocalDate periodStart,
            LocalDate periodEnd) {

        if (periodStart == null || periodEnd == null || periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Invalid analysis period.");
        }

        MenuIntelligenceAnalysisRun run = analysisRunRepository
                .findByMessAndPeriodForUpdate(messId, periodStart, periodEnd)
                .orElse(null);

        if (run != null) {

            if (run.getStatus() == AnalysisRunStatus.SUCCESS
                    || run.getStatus() == AnalysisRunStatus.IN_PROGRESS) {

                throw new IllegalStateException(
                        "An analysis run already exists for this mess and period.");
            }

            // Reuse a failed run.
            suggestionRepository.deleteAllByAnalysisRun_AnalysisRunId(
                    run.getAnalysisRunId());

            run.setStatus(AnalysisRunStatus.IN_PROGRESS);
            run.setStartedAt(LocalDateTime.now());
            run.setCompletedAt(null);
            run.setSuggestionCount(0);

            return analysisRunRepository.save(run).getAnalysisRunId();
        }

        Mess mess = messRepository.findById(messId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mess not found: " + messId));

        run = new MenuIntelligenceAnalysisRun();
        run.setMess(mess);
        run.setPeriodStart(periodStart);
        run.setPeriodEnd(periodEnd);
        run.setStatus(AnalysisRunStatus.IN_PROGRESS);
        run.setStartedAt(LocalDateTime.now());
        run.setSuggestionCount(0);

        return analysisRunRepository.save(run).getAnalysisRunId();
    }

    /**
     * Saves all validated suggestions and marks the run SUCCESS.
     * Both operations happen in one transaction.
     */
    @Transactional
    public void completeAnalysisRun(
            Long messId,
            Long analysisRunId,
            ValidatedMenuFeedbackSuggestions suggestions) {

        MenuIntelligenceAnalysisRun run =
                getOwnedRun(messId, analysisRunId);

        if (run.getStatus() != AnalysisRunStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only an in-progress analysis run can be completed.");
        }

        List<MenuIntelligenceSuggestion> entities =
                new ArrayList<>();

        for (ValidatedDishSuggestion dishSuggestion
                : suggestions.dishSuggestions()) {

            Dish dish = dishRepository.findByDishIdAndMess_MessId(
                    dishSuggestion.dishId(),
                    messId
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Dish does not belong to this mess: "
                            + dishSuggestion.dishId()));

            MenuIntelligenceSuggestion entity =
                    createSuggestion(
                            run,
                            dish,
                            dishSuggestion.observation(),
                            dishSuggestion.recommendation(),
                            dishSuggestion.priority()
                    );

            entities.add(entity);
        }

        for (OverallMealSuggestion overallSuggestion
                : suggestions.overallMealSuggestions()) {

            MenuIntelligenceSuggestion entity =
                    createSuggestion(
                            run,
                            null,
                            overallSuggestion.observation(),
                            overallSuggestion.recommendation(),
                            overallSuggestion.priority()
                    );

            entities.add(entity);
        }

        suggestionRepository.saveAll(entities);

        run.setSuggestionCount(entities.size());
        run.setStatus(AnalysisRunStatus.SUCCESS);
        run.setCompletedAt(LocalDateTime.now());

        analysisRunRepository.save(run);
    }

    /**
     * Marks an unfinished run as FAILED.
     * This method is intentionally separate from completion,
     * so a failed completion transaction can be recorded safely.
     */
    @Transactional
    public void markAnalysisRunFailed(
            Long messId,
            Long analysisRunId) {

        MenuIntelligenceAnalysisRun run =
                getOwnedRun(messId, analysisRunId);

        if (run.getStatus() == AnalysisRunStatus.IN_PROGRESS) {
            run.setStatus(AnalysisRunStatus.FAILED);
            run.setCompletedAt(LocalDateTime.now());

            analysisRunRepository.save(run);
        }
    }

    private MenuIntelligenceAnalysisRun getOwnedRun(
            Long messId,
            Long analysisRunId) {

        MenuIntelligenceAnalysisRun run =
                analysisRunRepository.findById(analysisRunId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Analysis run not found: " + analysisRunId));

        if (!run.getMess().getMessId().equals(messId)) {
            throw new IllegalArgumentException(
                    "Analysis run does not belong to this mess.");
        }

        return run;
    }

    private MenuIntelligenceSuggestion createSuggestion(
            MenuIntelligenceAnalysisRun run,
            Dish dish,
            String observation,
            String recommendation,
            com.smartmess.backend.enums.SuggestionPriority priority) {

        MenuIntelligenceSuggestion suggestion =
                new MenuIntelligenceSuggestion();

        suggestion.setMess(run.getMess());
        suggestion.setAnalysisRun(run);
        suggestion.setDish(dish);
        suggestion.setObservation(observation);
        suggestion.setRecommendation(recommendation);
        suggestion.setPriority(priority);
        suggestion.setPeriodStart(run.getPeriodStart());
        suggestion.setPeriodEnd(run.getPeriodEnd());

        return suggestion;
    }
}