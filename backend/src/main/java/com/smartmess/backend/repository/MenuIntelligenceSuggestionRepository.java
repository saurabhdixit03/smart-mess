package com.smartmess.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.MenuIntelligenceSuggestion;

public interface MenuIntelligenceSuggestionRepository
        extends JpaRepository<MenuIntelligenceSuggestion, Long> {

    List<MenuIntelligenceSuggestion> findAllByMess_MessIdOrderByCreatedAtDesc(
            Long messId
    );

    void deleteAllByAnalysisRun_AnalysisRunId(Long analysisRunId);

    /**
     * Retrieves suggestions belonging to the latest successful
     * analysis run for a particular mess.
     *
     * An empty successful run correctly returns an empty list,
     * rather than displaying suggestions from an older run.
     */
    @Query("""
            SELECT suggestion
            FROM MenuIntelligenceSuggestion suggestion
            WHERE suggestion.mess.messId = :messId
              AND suggestion.analysisRun.analysisRunId = (
                  SELECT MAX(run.analysisRunId)
                  FROM MenuIntelligenceAnalysisRun run
                  WHERE run.mess.messId = :messId
                    AND run.status = com.smartmess.backend.enums.AnalysisRunStatus.SUCCESS
              )
            ORDER BY
                CASE suggestion.priority
                    WHEN com.smartmess.backend.enums.SuggestionPriority.HIGH THEN 1
                    WHEN com.smartmess.backend.enums.SuggestionPriority.MEDIUM THEN 2
                    WHEN com.smartmess.backend.enums.SuggestionPriority.LOW THEN 3
                END,
                suggestion.createdAt DESC
            """)
    List<MenuIntelligenceSuggestion> findLatestSuggestionsByMessId(
            @Param("messId") Long messId
    );
}