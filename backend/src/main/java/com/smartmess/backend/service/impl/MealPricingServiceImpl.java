package com.smartmess.backend.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealPricingMapper;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.service.MealPricingService;
import com.smartmess.backend.service.NotificationService;

@Service
public class MealPricingServiceImpl
        implements MealPricingService {

    private final MealPricingRepository mealPricingRepository;
    private final MealPricingMapper mealPricingMapper;
    private final NotificationService notificationService;

    public MealPricingServiceImpl(
            MealPricingRepository mealPricingRepository,
            MealPricingMapper mealPricingMapper,
            NotificationService notificationService) {

        this.mealPricingRepository = mealPricingRepository;
        this.mealPricingMapper = mealPricingMapper;
        this.notificationService = notificationService;
    }

    @Override
    public MealPricingResponse getCurrentPricing() {

        MealPricing mealPricing =
                mealPricingRepository
                        .findTopByOrderByUpdatedAtDesc()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Meal pricing not configured."
                                )
                        );

        return mealPricingMapper.toResponse(
                mealPricing
        );
    }

    @Override
    public MealPricingResponse updatePricing(
            UpdateMealPricingRequest request) {

        MealPricing mealPricing =
                mealPricingRepository
                        .findTopByOrderByUpdatedAtDesc()
                        .orElseGet(MealPricing::new);

        boolean changed =
                hasPriceChanged(
                        mealPricing.getHalfMealPrice(),
                        request.halfMealPrice()
                )
                        || hasPriceChanged(
                                mealPricing.getFullMealPrice(),
                                request.fullMealPrice()
                        )
                        || hasPriceChanged(
                                mealPricing.getExtraRotiPrice(),
                                request.extraRotiPrice()
                        );

        if (!changed) {
            return mealPricingMapper.toResponse(
                    mealPricing
            );
        }

        mealPricingMapper.updateMealPricingFromRequest(
                request,
                mealPricing
        );

        MealPricing savedPricing =
                mealPricingRepository.save(
                        mealPricing
                );

        /*
         * Notify active customers only after
         * the meal pricing has actually changed.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MEAL_PRICING,
                "Meal Pricing Updated",
                buildPricingNotificationMessage(
                        savedPricing
                )
        );

        return mealPricingMapper.toResponse(
                savedPricing
        );
    }

    private boolean hasPriceChanged(
            BigDecimal currentPrice,
            BigDecimal newPrice) {

        if (currentPrice == null) {
            return newPrice != null;
        }

        if (newPrice == null) {
            return true;
        }

        return currentPrice.compareTo(
                newPrice
        ) != 0;
    }

    private String buildPricingNotificationMessage(
            MealPricing mealPricing) {

        return "Half Meal: ₹"
                + formatPrice(
                        mealPricing.getHalfMealPrice()
                )
                + " · Full Meal: ₹"
                + formatPrice(
                        mealPricing.getFullMealPrice()
                )
                + " · Extra Roti: ₹"
                + formatPrice(
                        mealPricing.getExtraRotiPrice()
                );
    }

    private String formatPrice(
            BigDecimal price) {

        return price.stripTrailingZeros()
                .toPlainString();
    }
}