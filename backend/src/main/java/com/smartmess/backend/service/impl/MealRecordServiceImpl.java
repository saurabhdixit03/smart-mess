package com.smartmess.backend.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMealRecordRequest;
import com.smartmess.backend.dto.response.CollectionQueueResponse;
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

    private final MealRecordRepository mealRecordRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseRepository mealResponseRepository;
    private final MealPricingRepository mealPricingRepository;
    private final MealRecordMapper mealRecordMapper;
    private final DashboardWebSocketService dashboardWebSocketService;

    // Used for mapping meal responses into the meal collection queue.
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

    @Override
    public MealRecordResponse createMealRecord(
            CreateMealRecordRequest request) {

        Long messId =
                customerSecurity.getCurrentMessId();

        // Load Customer within the authenticated mess

        Customer customer =
                customerRepository
                        .findByCustomerIdAndMess_MessId(
                                request.customerId(),
                                messId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + request.customerId()
                                ));

        // Customer Validation

        if (customer.getStatus() != CustomerStatus.ACTIVE) {

            throw new BusinessException(
                    "Only active customers can collect meals."
            );
        }

        // Load Menu within the authenticated mess

        Menu menu =
                menuRepository
                        .findByMenuIdAndMess_MessId(
                                request.menuId(),
                                messId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + request.menuId()
                                ));

        /*
         * An existing menu represents an operational meal session.
         *
         * Response cutoff restrictions apply only to customer meal responses.
         * The owner may still record meal collection for an existing menu,
         * including direct or walk-in meal collection.
         */

        // Load Meal Pricing for the authenticated mess

        MealPricing mealPricing =
                mealPricingRepository
                        .findByMess_MessId(messId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Meal pricing is not configured."
                                ));

        // Meal Pricing Validation

        if (mealPricing.getHalfMealPrice()
                .compareTo(BigDecimal.ZERO) <= 0
                || mealPricing.getFullMealPrice()
                        .compareTo(BigDecimal.ZERO) <= 0
                || mealPricing.getExtraRotiPrice()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Meal pricing is invalid. Please configure valid meal prices."
            );
        }

        // Load Meal Response (Optional)

        MealResponse mealResponse = null;

        if (request.mealResponseId() != null) {

            mealResponse =
                    mealResponseRepository
                            .findByMealResponseIdAndMess_MessId(
                                    request.mealResponseId(),
                                    messId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Meal response not found with ID: "
                                                    + request.mealResponseId()
                                    ));
        }

        // Business Validation

        if (mealResponse != null) {

            if (!mealResponse.getCustomer()
                    .getCustomerId()
                    .equals(customer.getCustomerId())) {

                throw new BusinessException(
                        "Meal response does not belong to the selected customer."
                );
            }

            if (!mealResponse.getMenu()
                    .getMenuId()
                    .equals(menu.getMenuId())) {

                throw new BusinessException(
                        "Meal response does not belong to the selected menu."
                );
            }

            if (mealRecordRepository
                    .findByMess_MessIdAndMealResponse(
                            messId,
                            mealResponse
                    )
                    .isPresent()) {

                throw new BusinessException(
                        "Meal has already been collected for this response."
                );
            }

        } else {

            if (mealRecordRepository
                    .existsByMess_MessIdAndCustomerAndMenu(
                            messId,
                            customer,
                            menu
                    )) {

                throw new BusinessException(
                        "Meal has already been collected for this customer and menu."
                );
            }
        }

        // Calculate Pricing

        BigDecimal mealPrice;

        if (request.mealOption() == MealOption.FULL) {
            mealPrice = mealPricing.getFullMealPrice();
        } else {
            mealPrice = mealPricing.getHalfMealPrice();
        }

        BigDecimal extraRotiPrice =
                mealPricing.getExtraRotiPrice();

        BigDecimal totalAmount =
                mealPrice.add(
                        extraRotiPrice.multiply(
                                BigDecimal.valueOf(
                                        request.extraRotiCount()
                                )
                        )
                );

        // Create Meal Record

        /*
         * Save the owner's final served meal and quantities.
         *
         * Response choices are optional prefill data.
         * Stored prices preserve the collection-time charge
         * even when the mess changes pricing later.
         */
        MealRecord mealRecord =
                MealRecord.builder()
                        .mess(menu.getMess())
                        .customer(customer)
                        .menu(menu)
                        .mealResponse(mealResponse)
                        .mealOption(request.mealOption())
                        .mealPrice(mealPrice)
                        .extraRotiCount(request.extraRotiCount())
                        .extraRotiPrice(extraRotiPrice)
                        .totalAmount(totalAmount)
                        .collectedAt(LocalDateTime.now(clock))
                        .build();

        // Save Meal Record

        MealRecord savedMealRecord =
                mealRecordRepository.save(mealRecord);

        dashboardWebSocketService.broadcastDashboard(
                menu.getMealSession()
        );

        // Return Response

        return mealRecordMapper.toResponse(
                savedMealRecord
        );
    }

    @Override
    public List<MealRecordResponse> getCustomerMealHistory(
            Long customerId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                customerRepository
                        .findByCustomerIdAndMess_MessId(
                                customerId,
                                messId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId
                                ));

        customerSecurity.checkCustomerAccess(
                customerId
        );

        List<MealRecord> mealRecords =
                mealRecordRepository
                        .findByMess_MessIdAndCustomerOrderByCollectedAtDesc(
                                messId,
                                customer
                        );

        return mealRecordMapper.toResponseList(
                mealRecords
        );
    }

    @Override
    public List<MealRecordResponse> getTodayMealRecords(
            MealSession mealSession) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Menu menu =
                findTodayMenu(messId, mealSession);

        List<MealRecord> mealRecords =
                mealRecordRepository.findByMess_MessIdAndMenu(
                        messId,
                        menu
                );

        return mealRecordMapper.toResponseList(
                mealRecords
        );
    }

    // Prefills accepted meal responses into the collection queue.
    // The final meal collection action is always performed by the owner.

    @Override
    public List<CollectionQueueResponse> getCollectionQueue(
            MealSession mealSession) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Menu menu =
                findTodayMenu(messId, mealSession);

        List<MealResponse> mealResponses =
                mealResponseRepository
                        .findCollectionQueueByMess(
                                messId,
                                menu
                        );

        return mealCollectionMapper.toResponseList(
                mealResponses
        );
    }

    /*
     * Resolves today's meal-session menu within one mess.
     * Preserves the existing missing-menu message.
     */
    private Menu findTodayMenu(
            Long messId,
            MealSession mealSession) {

        return menuRepository
                .findByMess_MessIdAndMenuDateAndMealSession(
                        messId,
                        LocalDate.now(clock),
                        mealSession
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Menu not found for today and session: "
                                        + mealSession
                        ));
    }
}