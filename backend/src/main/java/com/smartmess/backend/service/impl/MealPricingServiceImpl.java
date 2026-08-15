package com.smartmess.backend.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.UpdateMealPricingRequest;
import com.smartmess.backend.dto.response.MealPricingResponse;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.exception.ResourceNotFoundException;
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

    	MealPricing mealPricing =
    	        mealPricingRepository
    	                .findTopByOrderByUpdatedAtDesc()
    	                .orElseThrow(() ->
    	                        new ResourceNotFoundException(
    	                                "Meal pricing not configured."
    	                        ));

        return mealPricingMapper.toResponse(mealPricing);
    }

    @Override
    public MealPricingResponse updatePricing(UpdateMealPricingRequest request) {

        MealPricing mealPricing =
                mealPricingRepository
                        .findTopByOrderByUpdatedAtDesc()
                        .orElseGet(MealPricing::new);

        mealPricingMapper.updateMealPricingFromRequest(request, mealPricing);

        MealPricing savedPricing =
                mealPricingRepository.save(mealPricing);

        return mealPricingMapper.toResponse(savedPricing);
    }
}