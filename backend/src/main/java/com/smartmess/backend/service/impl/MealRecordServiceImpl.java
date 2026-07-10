package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealRecordMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.service.DashboardWebSocketService;
import com.smartmess.backend.service.MealRecordService;

@Service
public class MealRecordServiceImpl implements MealRecordService {

    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;
    private final MealPricingRepository mealPricingRepository;
    private final MealRecordMapper mealRecordMapper;
    private final DashboardWebSocketService dashboardWebSocketService;

    public MealRecordServiceImpl(
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository,
            MealPricingRepository mealPricingRepository,
            MealRecordMapper mealRecordMapper,
            DashboardWebSocketService dashboardWebSocketService) {

        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
        this.mealPricingRepository = mealPricingRepository;
        this.mealRecordMapper = mealRecordMapper;
        this.dashboardWebSocketService = dashboardWebSocketService;
    }

    @Override
    public MealRecordResponse createMealRecord(
            CreateMealRecordRequest request) {

        // Load Customer

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + request.customerId()));

        // Customer Validation

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active customers can collect meals.");
        }

        // Load Menu

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with ID: " + request.menuId()));

        // Load Meal Pricing

        MealPricing mealPricing = mealPricingRepository
                .findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Meal pricing is not configured."));

     // Meal Pricing Validation

        if (mealPricing.getHalfMealPrice().compareTo(BigDecimal.ZERO) <= 0
                || mealPricing.getFullMealPrice().compareTo(BigDecimal.ZERO) <= 0
                || mealPricing.getExtraRotiPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Meal pricing is invalid. Please configure valid meal prices.");
        }
        
        // Load Meal Response (Optional)

        MealResponse mealResponse = null;

        if (request.mealResponseId() != null) {

            mealResponse = mealResponseRepository
                    .findById(request.mealResponseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meal response not found with ID: "
                                    + request.mealResponseId()));
        }

     // Business Validation

        if (mealResponse != null) {

            if (!mealResponse.getCustomer().getCustomerId()
                    .equals(customer.getCustomerId())) {

                throw new BusinessException(
                        "Meal response does not belong to the selected customer.");
            }

            if (!mealResponse.getMenu().getMenuId()
                    .equals(menu.getMenuId())) {

                throw new BusinessException(
                        "Meal response does not belong to the selected menu.");
            }

            if (mealRecordRepository.findByMealResponse(mealResponse).isPresent()) {

                throw new BusinessException(
                        "Meal has already been collected for this response.");
            }

        } else {

            if (mealRecordRepository.existsByCustomerAndMenu(customer, menu)) {

                throw new BusinessException(
                        "Meal has already been collected for this customer and menu.");
            }
        }

     // Calculate Pricing

        BigDecimal mealPrice;

        if (request.mealOption() == MealOption.FULL) {

            mealPrice = mealPricing.getFullMealPrice();

        } else {

            mealPrice = mealPricing.getHalfMealPrice();
        }

        BigDecimal extraRotiPrice = mealPricing.getExtraRotiPrice();

        BigDecimal totalAmount =
                mealPrice.add(
                        extraRotiPrice.multiply(
                                BigDecimal.valueOf(request.extraRotiCount())));
        
        
     // Create Meal Record

        MealRecord mealRecord = MealRecord.builder()

                .customer(customer)

                .menu(menu)

                .mealResponse(mealResponse)

                .mealOption(request.mealOption())

                .mealPrice(mealPrice)

                .extraRotiCount(request.extraRotiCount())

                .extraRotiPrice(extraRotiPrice)

                .totalAmount(totalAmount)

                .collectedAt(LocalDateTime.now())

                .build();

        // Save Meal Record

        MealRecord savedMealRecord =
                mealRecordRepository.save(mealRecord);
        
        dashboardWebSocketService.broadcastDashboard(
                menu.getMealSession());
    
     // Return Response

        return mealRecordMapper.toResponse(savedMealRecord);
    }
    

    @Override
    public List<MealRecordResponse> getCustomerMealHistory(
            Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId));

        List<MealRecord> mealRecords =
                mealRecordRepository
                        .findByCustomerOrderByCollectedAtDesc(customer);

        return mealRecordMapper.toResponseList(mealRecords);
    }

    @Override
    public List<MealRecordResponse> getTodayMealRecords(
            MealSession mealSession) {

        Menu menu = menuRepository
                .findByMenuDateAndMealSession(
                        LocalDate.now(),
                        mealSession)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found for today and session: "
                                + mealSession));

        List<MealRecord> mealRecords =
                mealRecordRepository.findByMenu(menu);

        return mealRecordMapper.toResponseList(mealRecords);
    }
}