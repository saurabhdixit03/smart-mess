package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.MealResponseAvailabilityResponse;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.enums.MealSession;
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
public class MealResponseServiceImpl implements MealResponseService {

    private static final Logger log =
            LoggerFactory.getLogger(MealResponseServiceImpl.class);

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

    @Transactional
    @Override
    public MealResponseResponse submitMealResponse(
            Long customerId,
            SubmitMealResponseRequest request) {

        if (customerId == null || request == null
                || request.getMenuId() == null) {
            throw new BusinessException(
                    "Customer and menu are required."
            );
        }

        Long messId = customerSecurity.getCurrentMessId();

        /*
         * Acquire the same customer lock used by meal collection.
         * Response changes and collection are serialized.
         */
        Customer customer = customerRepository
                .findByCustomerIdAndMessIdForUpdate(customerId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));

        customerSecurity.checkCustomerAccess(customerId);

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active customers can submit or update meal responses."
            );
        }

        Menu menu = findMenu(request.getMenuId(), messId);

        if (mealRecordRepository.existsByMess_MessIdAndCustomerAndMenu(
                messId, customer, menu)) {
            throw new BusinessException(
                    "Meal response cannot be changed because the meal has already been recorded."
            );
        }

        LocalDateTime now = LocalDateTime.now(clock);
        validateResponseWindow(messId, menu, now);

        if (request.getResponseStatus() == null) {
            throw new BusinessException(
                    "Meal response status is required."
            );
        }

        if (request.getResponseStatus() == MealResponseStatus.ACCEPTED
                && request.getMealOption() == null) {
            throw new BusinessException(
                    "Meal option is required when response status is ACCEPTED."
            );
        }

        if (request.getResponseStatus() == MealResponseStatus.DECLINED
                && request.getMealOption() != null) {
            throw new BusinessException(
                    "Meal option must be empty when response status is DECLINED."
            );
        }

        Integer extraRotiCount = request.getExtraRotiCount();

        if (extraRotiCount == null
                || extraRotiCount < 0
                || extraRotiCount > 5) {
            throw new BusinessException(
                    "Extra roti count must be between 0 and 5."
            );
        }

        if (request.getResponseStatus() == MealResponseStatus.DECLINED
                && extraRotiCount > 0) {
            throw new BusinessException(
                    "Extra roti count must be zero when response status is DECLINED."
            );
        }

        MealResponse mealResponse = mealResponseRepository
                .findByMess_MessIdAndCustomerAndMenu(messId, customer, menu)
                .orElseGet(() -> {
                    MealResponse response = new MealResponse();
                    response.setCustomer(customer);
                    response.setMenu(menu);
                    response.setMess(menu.getMess());
                    return response;
                });

        mealResponseMapper.updateMealResponseFromRequest(
                request, mealResponse
        );

        mealResponse.setRespondedAt(now);

        MealResponse savedMealResponse =
                mealResponseRepository.saveAndFlush(mealResponse);

        broadcastAfterCommit(menu.getMealSession());

        return mealResponseMapper.toResponse(savedMealResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MealResponseResponse> getResponsesByMenu(Long menuId) {
        Long messId = customerSecurity.getCurrentMessId();
        Menu menu = findMenu(menuId, messId);

        List<MealResponse> responses =
                mealResponseRepository.findByMess_MessIdAndMenu(messId, menu);

        return mealResponseMapper.toResponseList(responses);
    }

    @Transactional(readOnly = true)
    @Override
    public MealResponseResponse getCustomerResponse(
            Long customerId,
            Long menuId) {

        customerSecurity.checkCustomerAccess(customerId);

        Long messId = customerSecurity.getCurrentMessId();
        Customer customer = findCustomer(customerId, messId);
        Menu menu = findMenu(menuId, messId);

        return mealResponseRepository
                .findByMess_MessIdAndCustomerAndMenu(messId, customer, menu)
                .map(mealResponseMapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public MealResponseAvailabilityResponse getResponseAvailability(
            Long menuId) {

        Long messId = customerSecurity.getCurrentMessId();
        Menu menu = findMenu(menuId, messId);

        Long customerId = customerSecurity.getCurrentUserId();
        Customer customer = findCustomer(customerId, messId);

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            return availability(
                    menu,
                    false,
                    "Only active customers can submit or update meal responses."
            );
        }

        if (mealRecordRepository.existsByMess_MessIdAndCustomerAndMenu(
                messId, customer, menu)) {
            return availability(menu, false, "Meal already recorded.");
        }

        LocalDateTime now = LocalDateTime.now(clock);

        if (!now.toLocalDate().equals(menu.getMenuDate())) {
            return availability(
                    menu,
                    false,
                    "Meal responses are only available for today's menu."
            );
        }

        MessSettings settings = getMessSettings(messId);
        LocalTime cutoffTime = getResponseCutoff(settings, menu);

        if (cutoffTime == null) {
            return availability(
                    menu, false, "Response cutoff is not configured."
            );
        }

        if (!now.toLocalTime().isBefore(cutoffTime)) {
            return availability(
                    menu,
                    false,
                    "Response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT) + "."
            );
        }

        return availability(menu, true, null);
    }

    private MealResponseAvailabilityResponse availability(
            Menu menu,
            boolean canRespond,
            String reason) {

        return new MealResponseAvailabilityResponse(
                menu.getMenuId(),
                menu.getMealSession(),
                canRespond,
                reason
        );
    }

    /*
     * Preserves today's-menu and session-cutoff rules.
     */
    private void validateResponseWindow(
            Long messId,
            Menu menu,
            LocalDateTime now) {

        LocalDate today = now.toLocalDate();

        if (!today.equals(menu.getMenuDate())) {
            throw new BusinessException(
                    "Meal responses can only be submitted for today's menu."
            );
        }

        MessSettings settings = getMessSettings(messId);
        LocalTime cutoffTime = getResponseCutoff(settings, menu);

        if (cutoffTime == null) {
            throw new BusinessException(
                    "Response cutoff time is not configured for this meal session."
            );
        }

        if (!now.toLocalTime().isBefore(cutoffTime)) {
            throw new BusinessException(
                    "Meal response cannot be submitted because the response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT) + "."
            );
        }
    }

    private Customer findCustomer(Long customerId, Long messId) {
        return customerRepository
                .findByCustomerIdAndMess_MessId(customerId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));
    }

    private Menu findMenu(Long menuId, Long messId) {
        return menuRepository
                .findByMenuIdAndMess_MessId(menuId, messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with ID: " + menuId
                ));
    }

    private MessSettings getMessSettings(Long messId) {
        return messSettingsRepository
                .findByMess_MessId(messId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mess settings not found."
                ));
    }

    private LocalTime getResponseCutoff(
            MessSettings settings,
            Menu menu) {

        return switch (menu.getMealSession()) {
            case LUNCH -> settings.getLunchResponseCutoff();
            case DINNER -> settings.getDinnerResponseCutoff();
        };
    }

    /*
     * A dashboard delivery failure must not turn a saved
     * response into an apparent failed submission.
     */
    private void broadcastAfterCommit(MealSession mealSession) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            dashboardWebSocketService.broadcastDashboard(
                                    mealSession
                            );
                        } catch (RuntimeException exception) {
                            log.error(
                                    "Dashboard broadcast failed after saving meal response for {}.",
                                    mealSession,
                                    exception
                            );
                        }
                    }
                }
        );
    }
}