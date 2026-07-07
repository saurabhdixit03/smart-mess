package com.smartmess.backend.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.mapper.MealPricingMapper;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.service.MealPricingService;

@Service
public class MealPricingServiceImpl implements MealPricingService {

    private final MealPricingRepository mealPricingRepository;
    private final MealPricingMapper mealPricingMapper;

    public MealPricingServiceImpl(MealPricingRepository mealPricingRepository,
                                  MealPricingMapper mealPricingMapper) {

        this.mealPricingRepository = mealPricingRepository;
        this.mealPricingMapper = mealPricingMapper;
    }

    @Override
    public MealPricingResponse getCurrentPricing() {

        MealPricing mealPricing = mealPricingRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {

                    MealPricing defaultPricing = new MealPricing();

                    defaultPricing.setHalfMealPrice(BigDecimal.ONE);
                    defaultPricing.setFullMealPrice(BigDecimal.ONE);
                    defaultPricing.setExtraRotiPrice(BigDecimal.ONE);

                    return mealPricingRepository.save(defaultPricing);
                });

        return mealPricingMapper.toResponse(mealPricing);
    }

    @Override
    public MealPricingResponse updatePricing(UpdateMealPricingRequest request) {

        MealPricing mealPricing = mealPricingRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {

                    MealPricing defaultPricing = new MealPricing();

                    defaultPricing.setHalfMealPrice(BigDecimal.ONE);
                    defaultPricing.setFullMealPrice(BigDecimal.ONE);
                    defaultPricing.setExtraRotiPrice(BigDecimal.ONE);

                    return mealPricingRepository.save(defaultPricing);
                });

        mealPricingMapper.updateMealPricingFromRequest(request, mealPricing);

        MealPricing updatedPricing = mealPricingRepository.save(mealPricing);

        return mealPricingMapper.toResponse(updatedPricing);
    }
}