package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    @Override
    public DashboardSummaryResponse getDashboardSummary(
            MealSession mealSession) {

        Long messId = customerSecurity.getCurrentMessId();

        Menu menu = menuRepository
                .findByMess_MessIdAndMenuDateAndMealSession(
                        messId,
                        LocalDate.now(clock),
                        mealSession
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found for today and session : "
                                + mealSession
                ));

        long activeCustomers = customerRepository
                .findAllByMess_MessIdAndStatus(
                        messId,
                        CustomerStatus.ACTIVE
                )
                .size();

        /*
         * Preserve stored responses for history.
         * Only currently active customers contribute to
         * daily dashboard counts and lists.
         */
        List<MealResponse> responses = mealResponseRepository
                .findByMess_MessIdAndMenu(messId, menu)
                .stream()
                .filter(mealResponse ->
                        mealResponse.getCustomer().getStatus()
                                == CustomerStatus.ACTIVE)
                .filter(mealResponse ->
                        messId.equals(
                                mealResponse.getCustomer()
                                        .getMess()
                                        .getMessId()
                        ))
                .toList();

        List<MealResponse> accepted = responses.stream()
                .filter(mealResponse ->
                        mealResponse.getResponseStatus()
                                == MealResponseStatus.ACCEPTED)
                .toList();

        long acceptedResponses = accepted.size();

        long declinedResponses = responses.stream()
                .filter(mealResponse ->
                        mealResponse.getResponseStatus()
                                == MealResponseStatus.DECLINED)
                .count();

        long pendingResponses = Math.max(
                0L,
                activeCustomers - acceptedResponses - declinedResponses
        );

        long expectedFullMeals = accepted.stream()
                .filter(mealResponse ->
                        mealResponse.getMealOption() == MealOption.FULL)
                .count();

        long expectedHalfMeals = accepted.stream()
                .filter(mealResponse ->
                        mealResponse.getMealOption() == MealOption.HALF)
                .count();

        long expectedExtraRotis = accepted.stream()
                .mapToLong(mealResponse ->
                        mealResponse.getExtraRotiCount() == null
                                ? 0L
                                : mealResponse.getExtraRotiCount())
                .sum();

        /*
         * Preserve the existing base-roti rule.
         * Both FULL and HALF meals include three base rotis.
         */
        long acceptedMeals = expectedFullMeals + expectedHalfMeals;
        long baseRotisRequired = acceptedMeals * 3;
        long totalRotisRequired = baseRotisRequired + expectedExtraRotis;

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
        response.setExpectedExtraRotis(expectedExtraRotis);
        response.setTotalRotisRequired(totalRotisRequired);

        /*
         * Preserve the dashboard's accepted-customer list
         * and its collected indicator.
         *
         * The separate meal collection page's pending queue
         * excludes collected customers.
         */
        List<DashboardCustomerResponse> customerQueue = accepted.stream()
                .sorted(Comparator.comparing(
                        (MealResponse mealResponse) ->
                                mealResponse.getCustomer().getFullName()
                ))
                .map(mealResponse ->
                        toDashboardCustomer(mealResponse, messId, menu))
                .toList();

        response.setCollectionQueue(customerQueue);

        List<DashboardCustomerResponse> recentActivities = responses.stream()
                .sorted(Comparator.comparing(
                        MealResponse::getRespondedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(3)
                .map(mealResponse -> {
                    DashboardCustomerResponse customer =
                            toDashboardCustomer(mealResponse, messId, menu);

                    customer.setRespondedAt(
                            mealResponse.getRespondedAt()
                    );

                    return customer;
                })
                .toList();

        response.setRecentActivities(recentActivities);

        return response;
    }

    private DashboardCustomerResponse toDashboardCustomer(
            MealResponse mealResponse,
            Long messId,
            Menu menu) {

        DashboardCustomerResponse customer =
                new DashboardCustomerResponse();

        customer.setMealResponseId(
                mealResponse.getMealResponseId()
        );

        customer.setCustomerId(
                mealResponse.getCustomer().getCustomerId()
        );

        customer.setCustomerName(
                mealResponse.getCustomer().getFullName()
        );

        customer.setResponseStatus(
                mealResponse.getResponseStatus()
        );

        customer.setMealOption(
                mealResponse.getMealOption()
        );

        customer.setExtraRotiCount(
                mealResponse.getExtraRotiCount()
        );

        /*
         * Detects collection through either the response queue
         * or manual collection without a linked response.
         */
        customer.setCollected(
                mealRecordRepository
                        .existsByMess_MessIdAndCustomerAndMenu(
                                messId,
                                mealResponse.getCustomer(),
                                menu
                        )
        );

        return customer;
    }
}