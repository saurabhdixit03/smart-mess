package com.smartmess.backend.entity;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.enums.SuggestionPriority;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "menu_intelligence_suggestions",
        indexes = {
                @Index(
                        name = "idx_menu_intelligence_mess_created",
                        columnList = "mess_id, created_at"
                ),
                @Index(
                        name = "idx_menu_intelligence_mess_dish",
                        columnList = "mess_id, dish_id"
                )
        }
)
public class MenuIntelligenceSuggestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long suggestionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private MenuIntelligenceAnalysisRun analysisRun;

    /*
     * NULL means this suggestion applies to the overall meal.
     * Non-NULL means it applies to a specific dish.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(name = "observation", nullable = false, length = 1000)
    private String observation;

    @Column(name = "recommendation", nullable = false, length = 1500)
    private String recommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private SuggestionPriority priority;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /*
     * Identifies the meal session when this suggestion is
     * specific to a meal. NULL for overall meal suggestions.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_session", length = 20)
    private MealSession mealSession;

}