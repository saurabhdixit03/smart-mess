package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.repository.MealFeedbackRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MenuIntelligenceService;
import com.smartmess.backend.dto.response.DishFeedbackAnalyticsResponse;
import com.smartmess.backend.repository.projection.DishFeedbackObservationProjection;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContext;
import com.smartmess.backend.service.intelligence.MenuIntelligenceContextBuilder;
@Service
public class MenuIntelligenceServiceImpl
        implements MenuIntelligenceService {

    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final CustomerSecurity customerSecurity;
    private final MealFeedbackRepository mealFeedbackRepository;
    private final MenuIntelligenceContextBuilder contextBuilder;

    public MenuIntelligenceServiceImpl(
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            MealFeedbackRepository mealFeedbackRepository,
            CustomerSecurity customerSecurity,
            MenuIntelligenceContextBuilder contextBuilder) {

        this.dishFeedbackRatingRepository =
                dishFeedbackRatingRepository;

        this.customerSecurity =
                customerSecurity;
        
        this.mealFeedbackRepository = 
        		mealFeedbackRepository;
        
        this.contextBuilder = contextBuilder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishRatingAnalyticsResponse> getDishRatingAnalytics() {

        Long messId =
                customerSecurity.getCurrentMessId();

        return dishFeedbackRatingRepository
                .getDishRatingAnalytics(messId)
                .stream()
                .map(projection -> new DishRatingAnalyticsResponse(
                        projection.getDishId(),
                        projection.getDishName(),
                        projection.getTotalRatings(),
                        projection.getAverageRating(),
                        projection.getOneStarCount(),
                        projection.getTwoStarCount(),
                        projection.getThreeStarCount(),
                        projection.getFourStarCount(),
                        projection.getFiveStarCount()
                ))
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DishFeedbackAnalyticsResponse> getDishFeedbackAnalytics() {
        Long messId = customerSecurity.getCurrentMessId();

        return mealFeedbackRepository
                .getDishFeedbackAnalytics(messId)
                .stream()
                .map(projection -> new DishFeedbackAnalyticsResponse(
                        projection.getDishId(),
                        projection.getDishName(),
                        projection.getTotalComments()
                ))
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DishFeedbackObservationProjection> getDishFeedbackObservations() {

        Long messId = customerSecurity.getCurrentMessId();

        return dishFeedbackRatingRepository
                .getDishFeedbackObservations(messId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MenuIntelligenceContext buildIntelligenceContext() {
        Long messId = customerSecurity.getCurrentMessId();
        return contextBuilder.build(messId);
    }
}