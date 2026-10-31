package com.smartmess.backend.service.intelligence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.projection.DishFeedbackObservationProjection;

@Component
public class MenuIntelligenceContextBuilder {

    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final DishIntelligenceCalculator dishIntelligenceCalculator;

    public MenuIntelligenceContextBuilder(
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            DishIntelligenceCalculator dishIntelligenceCalculator) {

        this.dishFeedbackRatingRepository = dishFeedbackRatingRepository;
        this.dishIntelligenceCalculator = dishIntelligenceCalculator;
    }

    public MenuIntelligenceContext build(Long messId) {

        List<DishFeedbackObservationProjection> projections =
                dishFeedbackRatingRepository
                        .getDishFeedbackObservations(messId);

        List<MenuIntelligenceObservation> observations =
                projections.stream()
                        .map(projection -> new MenuIntelligenceObservation(
                                projection.getMealFeedbackId(),
                                projection.getMenuId(),
                                projection.getMenuDate(),
                                projection.getMealSession(),
                                projection.getDishId(),
                                projection.getDishName(),
                                projection.getRating(),
                                projection.getComment()
                        ))
                        .toList();

        List<DishIntelligenceSummary> dishSummaries =
                dishIntelligenceCalculator.calculate(observations);

        return new MenuIntelligenceContext(
                messId,
                observations,
                dishSummaries
        );
    }
}