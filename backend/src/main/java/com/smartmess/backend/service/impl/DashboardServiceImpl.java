package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.response.DashboardCustomerResponse;
import com.smartmess.backend.dto.response.DashboardSummaryResponse;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;
    private final MealRecordRepository mealRecordRepository;
    private final Clock clock;
    private final CustomerSecurity customerSecurity;

    public DashboardServiceImpl(
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository,
            MealRecordRepository mealRecordRepository,
            Clock clock,
            CustomerSecurity customerSecurity) {

        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.clock = clock;
        this.customerSecurity = customerSecurity;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary(
            MealSession mealSession) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Menu menu = menuRepository
                .findByMess_MessIdAndMenuDateAndMealSession(
                        messId,
                        LocalDate.now(clock),
                        mealSession
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu not found for today and session : "
                                        + mealSession
                        ));

        long activeCustomers =
                customerRepository
                        .findAllByMess_MessIdAndStatus(
                                messId,
                                CustomerStatus.ACTIVE
                        )
                        .size();

        long acceptedResponses =
                mealResponseRepository
                        .countByMess_MessIdAndMenuAndResponseStatus(
                                messId,
                                menu,
                                MealResponseStatus.ACCEPTED
                        );

        long declinedResponses =
                mealResponseRepository
                        .countByMess_MessIdAndMenuAndResponseStatus(
                                messId,
                                menu,
                                MealResponseStatus.DECLINED
                        );

        long pendingResponses =
                activeCustomers
                        - acceptedResponses
                        - declinedResponses;

        long expectedFullMeals =
                mealResponseRepository
                        .countByMess_MessIdAndMenuAndMealOption(
                                messId,
                                menu,
                                MealOption.FULL
                        );

        long expectedHalfMeals =
                mealResponseRepository
                        .countByMess_MessIdAndMenuAndMealOption(
                                messId,
                                menu,
                                MealOption.HALF
                        );

        Long expectedExtraRotis =
                mealResponseRepository.getTotalExtraRotisByMess(
                        messId,
                        menu
                );

        long acceptedMeals =
                expectedFullMeals + expectedHalfMeals;

        long baseRotisRequired =
                acceptedMeals * 3;

        long totalRotisRequired =
                baseRotisRequired
                        + (expectedExtraRotis == null
                                ? 0L
                                : expectedExtraRotis);

        List<MealResponse> responses =
                mealResponseRepository.findByMess_MessIdAndMenu(
                        messId,
                        menu
                );

        DashboardSummaryResponse response =
                new DashboardSummaryResponse();

        response.setMenuDate(menu.getMenuDate());
        response.setMealSession(menu.getMealSession());
        response.setActiveCustomers(activeCustomers);

        response.setMenuId(menu.getMenuId());
        response.setAcceptedResponses(acceptedResponses);
        response.setDeclinedResponses(declinedResponses);
        response.setPendingResponses(pendingResponses);

        response.setExpectedFullMeals(expectedFullMeals);
        response.setExpectedHalfMeals(expectedHalfMeals);
        response.setBaseRotisRequired(baseRotisRequired);

        response.setExpectedExtraRotis(
                expectedExtraRotis == null
                        ? 0L
                        : expectedExtraRotis
        );

        response.setTotalRotisRequired(totalRotisRequired);

        /*
         * Customer Collection Queue
         */
        List<DashboardCustomerResponse> customerQueue =
                responses.stream()
                        .filter(mealResponse ->
                                mealResponse.getResponseStatus()
                                        == MealResponseStatus.ACCEPTED)
                        .sorted(Comparator.comparing(
                                mealResponse ->
                                        mealResponse.getCustomer().getFullName()
                        ))
                        .map(mealResponse -> {

                            DashboardCustomerResponse customer =
                                    new DashboardCustomerResponse();

                            customer.setMealResponseId(
                                    mealResponse.getMealResponseId());

                            customer.setCustomerId(
                                    mealResponse.getCustomer().getCustomerId());

                            customer.setCustomerName(
                                    mealResponse.getCustomer().getFullName());

                            customer.setResponseStatus(
                                    mealResponse.getResponseStatus());

                            customer.setMealOption(
                                    mealResponse.getMealOption());

                            customer.setExtraRotiCount(
                                    mealResponse.getExtraRotiCount());

                            customer.setCollected(
                                    mealRecordRepository
                                            .existsByMess_MessIdAndCustomerAndMenu(
                                                    messId,
                                                    mealResponse.getCustomer(),
                                                    menu
                                            )
                            );

                            return customer;
                        })
                        .toList();

        response.setCollectionQueue(customerQueue);

        List<DashboardCustomerResponse> recentActivities =
                responses.stream()
                        .sorted(
                                Comparator.comparing(
                                        MealResponse::getRespondedAt
                                ).reversed()
                        )
                        .limit(3)
                        .map(mealResponse -> {

                            DashboardCustomerResponse customer =
                                    new DashboardCustomerResponse();

                            customer.setMealResponseId(
                                    mealResponse.getMealResponseId());

                            customer.setCustomerId(
                                    mealResponse.getCustomer().getCustomerId());

                            customer.setCustomerName(
                                    mealResponse.getCustomer().getFullName());

                            customer.setResponseStatus(
                                    mealResponse.getResponseStatus());

                            customer.setMealOption(
                                    mealResponse.getMealOption());

                            customer.setExtraRotiCount(
                                    mealResponse.getExtraRotiCount());

                            customer.setRespondedAt(
                                    mealResponse.getRespondedAt());

                            customer.setCollected(
                                    mealRecordRepository
                                            .existsByMess_MessIdAndCustomerAndMenu(
                                                    messId,
                                                    mealResponse.getCustomer(),
                                                    menu
                                            )
                            );

                            return customer;
                        })
                        .toList();

        response.setRecentActivities(
                recentActivities
        );

        return response;
    }
}