package com.smartmess.backend.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;

    public DashboardServiceImpl(
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository) {

        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary(MealSession mealSession) {

        Menu menu = menuRepository
                .findByMenuDateAndMealSession(
                        LocalDate.now(),
                        mealSession
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu not found for today and session : " + mealSession
                        ));

        long activeCustomers =
                customerRepository.countByStatus(CustomerStatus.ACTIVE);

        long acceptedResponses =
                mealResponseRepository.countByMenuAndResponseStatus(
                        menu,
                        MealResponseStatus.ACCEPTED
                );

        long declinedResponses =
                mealResponseRepository.countByMenuAndResponseStatus(
                        menu,
                        MealResponseStatus.DECLINED
                );

        long pendingResponses =
                activeCustomers
                        - acceptedResponses
                        - declinedResponses;

        long expectedFullMeals =
                mealResponseRepository.countByMenuAndMealOption(
                        menu,
                        MealOption.FULL
                );

        long expectedHalfMeals =
                mealResponseRepository.countByMenuAndMealOption(
                        menu,
                        MealOption.HALF
                );

        Long expectedExtraRotis =
                mealResponseRepository.getTotalExtraRotis(menu);
        
        long acceptedMeals =
                expectedFullMeals + expectedHalfMeals;

        long totalRotisRequired =
                (acceptedMeals * 3)
                + expectedExtraRotis;

        DashboardSummaryResponse response =
                new DashboardSummaryResponse();

        response.setMenuDate(menu.getMenuDate());
        response.setMealSession(menu.getMealSession());

        response.setActiveCustomers(activeCustomers);

        response.setAcceptedResponses(acceptedResponses);
        response.setDeclinedResponses(declinedResponses);
        response.setPendingResponses(pendingResponses);

        response.setExpectedFullMeals(expectedFullMeals);
        response.setExpectedHalfMeals(expectedHalfMeals);
        response.setTotalRotisRequired(totalRotisRequired);
        
        response.setExpectedExtraRotis(
                expectedExtraRotis == null ? 0L : expectedExtraRotis
                		
        
        );

        return response;
    }

}