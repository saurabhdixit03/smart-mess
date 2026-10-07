package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.CollectionCustomerResponse;
import com.smartmess.backend.dto.response.CollectionQueueResponse;
import com.smartmess.backend.dto.response.MealRecordResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealPricing;
import com.smartmess.backend.entity.MealRecord;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealCollectionMapper;
import com.smartmess.backend.mapper.MealRecordMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MealRecordRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DashboardWebSocketService;
import com.smartmess.backend.service.MealRecordService;

@Service
public class MealRecordServiceImpl implements MealRecordService {

    private static final Logger log =
            LoggerFactory.getLogger(MealRecordServiceImpl.class);

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("99999999.99");

    private static final int SEARCH_RESULT_LIMIT = 50;

    private static final int MAX_SEARCH_LENGTH = 100;

    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;
    private final MealPricingRepository mealPricingRepository;
    private final MealRecordMapper mealRecordMapper;
    private final DashboardWebSocketService dashboardWebSocketService;
    private final MealCollectionMapper mealCollectionMapper;
    private final CustomerSecurity customerSecurity;
    private final Clock clock;

    public MealRecordServiceImpl(
            MealRecordRepository mealRecordRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseRepository mealResponseRepository,
            MealPricingRepository mealPricingRepository,
            MealRecordMapper mealRecordMapper,
            DashboardWebSocketService dashboardWebSocketService,
            MealCollectionMapper mealCollectionMapper,
            CustomerSecurity customerSecurity,
            Clock clock) {

        this.mealRecordRepository = mealRecordRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseRepository = mealResponseRepository;
        this.mealPricingRepository = mealPricingRepository;
        this.mealRecordMapper = mealRecordMapper;
        this.dashboardWebSocketService = dashboardWebSocketService;
        this.mealCollectionMapper = mealCollectionMapper;
        this.customerSecurity = customerSecurity;
        this.clock = clock;
    }

    @Transactional
    @Override
    public MealRecordResponse createMealRecord(
            CreateMealRecordRequest request) {

        requireOwner();
        validateRequest(request);

        Long messId = customerSecurity.getCurrentMessId();

        Customer customer = customerRepository
                .findByCustomerIdAndMessIdForUpdate(
                        request.customerId(),
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: "
                                + request.customerId()
                ));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active customers can collect meals."
            );
        }

        Menu menu = menuRepository
                .findByMenuIdAndMess_MessId(
                        request.menuId(),
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found with ID: "
                                + request.menuId()
                ));

        LocalDateTime collectedAt = LocalDateTime.now(clock);

        if (!collectedAt.toLocalDate().equals(menu.getMenuDate())) {
            throw new BusinessException(
                    "Meals can only be recorded for today's menu."
            );
        }

        /*
         * Applies to both walk-in and response-linked collections.
         * The customer lock serializes concurrent collection requests.
         * The database unique constraint provides additional protection.
         */
        if (mealRecordRepository.existsByMess_MessIdAndCustomerAndMenu(
                messId,
                customer,
                menu
        )) {
            throw new BusinessException(
                    "Meal has already been collected for this customer and menu."
            );
        }

        MealResponse mealResponse;

        if (request.mealResponseId() != null) {
            mealResponse = mealResponseRepository
                    .findByMealResponseIdAndMess_MessId(
                            request.mealResponseId(),
                            messId
                    )
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meal response not found with ID: "
                                    + request.mealResponseId()
                    ));

            if (!mealResponse.getCustomer().getCustomerId()
                    .equals(customer.getCustomerId())) {
                throw new BusinessException(
                        "Meal response does not belong to the selected customer."
                );
            }

            if (!mealResponse.getMenu().getMenuId()
                    .equals(menu.getMenuId())) {
                throw new BusinessException(
                        "Meal response does not belong to the selected menu."
                );
            }
        } else {
            /*
             * A response may have been submitted after customer search.
             * Link it when available without creating a response for
             * customers who genuinely have none.
             */
            mealResponse = mealResponseRepository
                    .findByMess_MessIdAndCustomerAndMenu(
                            messId,
                            customer,
                            menu
                    )
                    .orElse(null);
        }

        /*
         * Use pricing effective at collection time.
         * Future price changes are excluded.
         */
        MealPricing pricing = mealPricingRepository
                .findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        messId,
                        collectedAt
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configure meal prices in Settings before collecting meals."
                ));

        validatePrice(pricing.getHalfMealPrice());
        validatePrice(pricing.getFullMealPrice());
        validatePrice(pricing.getExtraRotiPrice());

        BigDecimal mealPrice =
                request.mealOption() == MealOption.FULL
                        ? pricing.getFullMealPrice()
                        : pricing.getHalfMealPrice();

        BigDecimal extraRotiPrice = pricing.getExtraRotiPrice();

        BigDecimal totalAmount = mealPrice.add(
                extraRotiPrice.multiply(
                        BigDecimal.valueOf(request.extraRotiCount())
                )
        );

        if (totalAmount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessException(
                    "The meal amount exceeds the supported limit."
            );
        }

        /*
         * Save actual served choices and collection-time prices.
         * A response is optional and provides only prefill information.
         */
        MealRecord record = MealRecord.builder()
                .mess(menu.getMess())
                .customer(customer)
                .menu(menu)
                .mealResponse(mealResponse)
                .mealOption(request.mealOption())
                .mealPrice(mealPrice)
                .extraRotiCount(request.extraRotiCount())
                .extraRotiPrice(extraRotiPrice)
                .totalAmount(totalAmount)
                .collectedAt(collectedAt)
                .build();

        MealRecord savedRecord =
                mealRecordRepository.saveAndFlush(record);

        broadcastAfterCommit(menu.getMealSession());

        return mealRecordMapper.toResponse(savedRecord);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MealRecordResponse> getCustomerMealHistory(
            Long customerId) {

        Long messId = customerSecurity.getCurrentMessId();

        Customer customer = customerRepository
                .findByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));

        customerSecurity.checkCustomerAccess(customerId);

        return mealRecordMapper.toResponseList(
                mealRecordRepository
                        .findByMess_MessIdAndCustomerOrderByCollectedAtDesc(
                                messId,
                                customer
                        )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<MealRecordResponse> getTodayMealRecords(
            MealSession mealSession) {

        requireOwner();

        Long messId = customerSecurity.getCurrentMessId();
        Menu menu = findTodayMenu(messId, mealSession);

        return mealRecordMapper.toResponseList(
                mealRecordRepository.findByMess_MessIdAndMenu(
                        messId,
                        menu
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<CollectionQueueResponse> getCollectionQueue(
            MealSession mealSession) {

        requireOwner();

        Long messId = customerSecurity.getCurrentMessId();
        Menu menu = findTodayMenu(messId, mealSession);

        /*
         * The default queue remains limited to accepted responses
         * from active customers who have not collected their meal.
         */
        return mealCollectionMapper.toResponseList(
                mealResponseRepository.findCollectionQueueByMess(
                        messId,
                        menu
                )
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<CollectionCustomerResponse> searchCollectionCustomers(
            MealSession mealSession,
            String search) {

        requireOwner();

        if (mealSession == null) {
            throw new BusinessException(
                    "Meal session is required."
            );
        }

        String keyword = search == null ? "" : search.strip();

        if (keyword.isEmpty()) {
            return List.of();
        }

        if (keyword.length() > MAX_SEARCH_LENGTH) {
            throw new BusinessException(
                    "Customer search must not exceed "
                            + MAX_SEARCH_LENGTH
                            + " characters."
            );
        }

        Long messId = customerSecurity.getCurrentMessId();
        Menu menu = findTodayMenu(messId, mealSession);

        List<Customer> customers = customerRepository
                .searchActiveCustomersForCollection(
                        messId,
                        buildSearchPattern(keyword),
                        PageRequest.of(0, SEARCH_RESULT_LIMIT)
                );

        if (customers.isEmpty()) {
            return List.of();
        }

        List<Long> customerIds = customers.stream()
                .map(Customer::getCustomerId)
                .toList();

        Map<Long, MealResponse> responsesByCustomer = new HashMap<>();

        for (MealResponse response : mealResponseRepository
                .findForCollectionSearch(
                        messId,
                        menu,
                        customerIds
                )) {

            responsesByCustomer.put(
                    response.getCustomer().getCustomerId(),
                    response
            );
        }

        Set<Long> collectedCustomerIds = new HashSet<>(
                mealRecordRepository.findCollectedCustomerIdsForMenu(
                        messId,
                        menu,
                        customerIds
                )
        );

        return customers.stream()
                .map(customer -> {
                    MealResponse response = responsesByCustomer.get(
                            customer.getCustomerId()
                    );

                    /*
                     * Accepted responses prefill the requested choices.
                     * No response or a declined response starts with
                     * Full Meal and no extra rotis for owner review.
                     */
                    boolean accepted = response != null
                            && response.getResponseStatus()
                                    == MealResponseStatus.ACCEPTED;

                    MealOption mealOption =
                            accepted && response.getMealOption() != null
                                    ? response.getMealOption()
                                    : MealOption.FULL;

                    Integer extraRotiCount =
                            accepted && response.getExtraRotiCount() != null
                                    ? response.getExtraRotiCount()
                                    : 0;

                    return new CollectionCustomerResponse(
                            customer.getCustomerId(),
                            customer.getFullName(),
                            customer.getMobileNumber(),
                            menu.getMenuId(),
                            response == null
                                    ? null
                                    : response.getMealResponseId(),
                            response == null
                                    ? null
                                    : response.getResponseStatus(),
                            mealOption,
                            extraRotiCount,
                            collectedCustomerIds.contains(
                                    customer.getCustomerId()
                            )
                    );
                })
                .toList();
    }

    private String buildSearchPattern(String keyword) {

        /*
         * Treat SQL LIKE wildcard characters as literal search text.
         * Matches the repository's ESCAPE '!' clause.
         */
        String escaped = keyword.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escaped + "%";
    }

    private Menu findTodayMenu(
            Long messId,
            MealSession mealSession) {

        if (mealSession == null) {
            throw new BusinessException(
                    "Meal session is required."
            );
        }

        return menuRepository
                .findByMess_MessIdAndMenuDateAndMealSession(
                        messId,
                        LocalDate.now(clock),
                        mealSession
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu not found for today and session: "
                                + mealSession
                ));
    }

    private void validateRequest(CreateMealRecordRequest request) {

        if (request == null
                || request.customerId() == null
                || request.menuId() == null
                || request.mealOption() == null) {
            throw new BusinessException(
                    "Customer, menu and meal option are required."
            );
        }

        if (request.customerId() <= 0 || request.menuId() <= 0) {
            throw new BusinessException(
                    "Customer and menu IDs must be positive."
            );
        }

        if (request.mealResponseId() != null
                && request.mealResponseId() <= 0) {
            throw new BusinessException(
                    "Meal response ID must be positive."
            );
        }

        if (request.extraRotiCount() == null
                || request.extraRotiCount() < 0
                || request.extraRotiCount() > 5) {
            throw new BusinessException(
                    "Extra roti count must be between 0 and 5."
            );
        }
    }

    private void validatePrice(BigDecimal price) {

        if (price == null
                || price.signum() <= 0
                || price.compareTo(MAX_AMOUNT) > 0
                || price.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(
                    "Meal pricing is invalid. Please configure valid meal prices."
            );
        }
    }

    private void requireOwner() {

        if (customerSecurity.getCurrentUserRole() != UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Only mess owners can manage meal collection."
            );
        }
    }

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
                            log.warn(
                                    "Dashboard delivery failed after meal collection for {}",
                                    mealSession,
                                    exception
                            );
                        }
                    }
                }
        );
    }
}