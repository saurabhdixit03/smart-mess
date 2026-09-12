package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.MealResponseAvailabilityResponse;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealResponseMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DashboardWebSocketService;
import com.smartmess.backend.service.MealResponseService;

@Service
public class MealResponseServiceImpl
        implements MealResponseService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a");

    private final MealResponseRepository mealResponseRepository;
    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MessSettingsRepository messSettingsRepository;
    private final MealResponseMapper mealResponseMapper;
    private final DashboardWebSocketService dashboardWebSocketService;
    private final CustomerSecurity customerSecurity;
    private final Clock clock;

    public MealResponseServiceImpl(
            MealResponseRepository mealResponseRepository,
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MessSettingsRepository messSettingsRepository,
            MealResponseMapper mealResponseMapper,
            DashboardWebSocketService dashboardWebSocketService,
            CustomerSecurity customerSecurity,
            Clock clock) {

        this.mealResponseRepository = mealResponseRepository;
        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.messSettingsRepository = messSettingsRepository;
        this.mealResponseMapper = mealResponseMapper;
        this.dashboardWebSocketService = dashboardWebSocketService;
        this.customerSecurity = customerSecurity;
        this.clock = clock;
    }

    @Override
    public MealResponseResponse submitMealResponse(
            Long customerId,
            SubmitMealResponseRequest request) {

        // Customer can only submit for themselves.
        customerSecurity.checkCustomerAccess(customerId);

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                )
                        );

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active customers can submit or update meal responses."
            );
        }

        Menu menu =
                menuRepository.findById(request.getMenuId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + request.getMenuId()
                                )
                        );

        if (mealRecordRepository.existsByCustomerAndMenu(
                customer,
                menu
        )) {
            throw new BusinessException(
                    "Meal response cannot be changed because the meal has already been recorded."
            );
        }

        validateResponseWindow(menu);

        MealResponse mealResponse =
                mealResponseRepository
                        .findByCustomerAndMenu(
                                customer,
                                menu
                        )
                        .orElseGet(() -> {

                            MealResponse response =
                                    new MealResponse();

                            response.setCustomer(
                                    customer
                            );

                            response.setMenu(
                                    menu
                            );

                            return response;
                        });

        // Business Validation

        if (request.getResponseStatus()
                == MealResponseStatus.ACCEPTED
                && request.getMealOption() == null) {

            throw new BusinessException(
                    "Meal option is required when response status is ACCEPTED."
            );
        }

        if (request.getResponseStatus()
                == MealResponseStatus.DECLINED
                && request.getMealOption() != null) {

            throw new BusinessException(
                    "Meal option must be empty when response status is DECLINED."
            );
        }

        if (request.getResponseStatus()
                == MealResponseStatus.DECLINED
                && request.getExtraRotiCount() > 0) {

            throw new BusinessException(
                    "Extra roti count must be zero when response status is DECLINED."
            );
        }

        mealResponseMapper
                .updateMealResponseFromRequest(
                        request,
                        mealResponse
                );

        mealResponse.setRespondedAt(
                LocalDateTime.now(clock)
        );

        MealResponse savedMealResponse =
                mealResponseRepository.save(
                        mealResponse
                );

        dashboardWebSocketService.broadcastDashboard(
                menu.getMealSession()
        );

        return mealResponseMapper.toResponse(
                savedMealResponse
        );
    }

    @Override
    public List<MealResponseResponse> getResponsesByMenu(
            Long menuId) {

        Menu menu =
                menuRepository.findById(menuId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + menuId
                                )
                        );

        List<MealResponse> mealResponses =
                mealResponseRepository.findByMenu(
                        menu
                );

        return mealResponseMapper.toResponseList(
                mealResponses
        );
    }

    @Override
    public MealResponseResponse getCustomerResponse(
            Long customerId,
            Long menuId) {

        // OWNER can access any customer.
        // CUSTOMER can access only their own response.
        customerSecurity.checkCustomerAccess(
                customerId
        );

        return mealResponseRepository
                .findByCustomerCustomerIdAndMenuMenuId(
                        customerId,
                        menuId
                )
                .map(
                        mealResponseMapper::toResponse
                )
                .orElse(null);
    }

    @Override
    public MealResponseAvailabilityResponse getResponseAvailability(
            Long menuId) {

        Menu menu =
                menuRepository.findById(menuId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + menuId
                                )
                        );

        Long customerId =
                customerSecurity.getCurrentUserId();

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                )
                        );

        if (mealRecordRepository.existsByCustomerAndMenu(
                customer,
                menu
        )) {
            return new MealResponseAvailabilityResponse(
                    menu.getMenuId(),
                    menu.getMealSession(),
                    false,
                    "Meal already recorded."
            );
        }

        LocalDate today =
                LocalDate.now(clock);

        if (!today.equals(menu.getMenuDate())) {

            return new MealResponseAvailabilityResponse(
                    menu.getMenuId(),
                    menu.getMealSession(),
                    false,
                    "Meal responses are only available for today's menu."
            );
        }

        MessSettings settings =
                getMessSettings();

        LocalTime cutoffTime =
                getResponseCutoff(
                        settings,
                        menu
                );

        if (cutoffTime == null) {

            return new MealResponseAvailabilityResponse(
                    menu.getMenuId(),
                    menu.getMealSession(),
                    false,
                    "Response cutoff is not configured."
            );
        }

        LocalTime currentTime =
                LocalTime.now(clock);

        if (!currentTime.isBefore(cutoffTime)) {

            return new MealResponseAvailabilityResponse(
                    menu.getMenuId(),
                    menu.getMealSession(),
                    false,
                    "Response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT)
                            + "."
            );
        }

        return new MealResponseAvailabilityResponse(
                menu.getMenuId(),
                menu.getMealSession(),
                true,
                null
        );
    }

    /*
     * Customers can submit or update responses only for today's menu
     * and only before the configured cutoff time for that meal session.
     */
    private void validateResponseWindow(
            Menu menu) {

        LocalDate today =
                LocalDate.now(clock);

        if (!today.equals(menu.getMenuDate())) {

            throw new BusinessException(
                    "Meal responses can only be submitted for today's menu."
            );
        }

        MessSettings settings =
                getMessSettings();

        LocalTime cutoffTime =
                getResponseCutoff(
                        settings,
                        menu
                );

        if (cutoffTime == null) {

            throw new BusinessException(
                    "Response cutoff time is not configured for this meal session."
            );
        }

        LocalTime currentTime =
                LocalTime.now(clock);

        if (!currentTime.isBefore(cutoffTime)) {

            throw new BusinessException(
                    "Meal response cannot be submitted because the response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT)
                            + "."
            );
        }
    }

    private MessSettings getMessSettings() {

        return messSettingsRepository
                .findTopByOrderBySettingsIdAsc()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mess settings not found."
                        )
                );
    }

    private LocalTime getResponseCutoff(
            MessSettings settings,
            Menu menu) {

        return switch (menu.getMealSession()) {

            case LUNCH ->
                    settings.getLunchResponseCutoff();

            case DINNER ->
                    settings.getDinnerResponseCutoff();
        };
    }
}