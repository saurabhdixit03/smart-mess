package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.response.DishRatingAnalyticsResponse;
import com.smartmess.backend.repository.DishFeedbackRatingRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MenuIntelligenceService;

@Service
public class MenuIntelligenceServiceImpl
        implements MenuIntelligenceService {

    private final DishFeedbackRatingRepository dishFeedbackRatingRepository;
    private final CustomerSecurity customerSecurity;

    public MenuIntelligenceServiceImpl(
            DishFeedbackRatingRepository dishFeedbackRatingRepository,
            CustomerSecurity customerSecurity) {

        this.dishFeedbackRatingRepository =
                dishFeedbackRatingRepository;

        this.customerSecurity =
                customerSecurity;
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
}