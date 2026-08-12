package com.smartmess.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.SubmitMealResponseRequest;
import com.smartmess.backend.dto.response.MealResponseResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MealResponseStatus;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MealResponseMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MealResponseRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DashboardWebSocketService;
import com.smartmess.backend.service.MealResponseService;

@Service
public class MealResponseServiceImpl implements MealResponseService {

    private final MealResponseRepository mealResponseRepository;
    private final CustomerRepository customerRepository;
    private final MenuRepository menuRepository;
    private final MealResponseMapper mealResponseMapper;
    private final DashboardWebSocketService dashboardWebSocketService;
    private final CustomerSecurity customerSecurity;

    public MealResponseServiceImpl(
            MealResponseRepository mealResponseRepository,
            CustomerRepository customerRepository,
            MenuRepository menuRepository,
            MealResponseMapper mealResponseMapper,
            DashboardWebSocketService dashboardWebSocketService,
            CustomerSecurity customerSecurity) {

        this.mealResponseRepository = mealResponseRepository;
        this.customerRepository = customerRepository;
        this.menuRepository = menuRepository;
        this.mealResponseMapper = mealResponseMapper;
        this.dashboardWebSocketService = dashboardWebSocketService;
        this.customerSecurity = customerSecurity;
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
                                                + customerId));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active customers can submit or update meal responses.");
        }

        Menu menu =
                menuRepository.findById(request.getMenuId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + request.getMenuId()));

        MealResponse mealResponse =
                mealResponseRepository
                        .findByCustomerAndMenu(customer, menu)
                        .orElseGet(() -> {

                            MealResponse response =
                                    new MealResponse();

                            response.setCustomer(customer);
                            response.setMenu(menu);

                            return response;
                        });

        // Business Validation

        if (request.getResponseStatus()
                == MealResponseStatus.ACCEPTED
                && request.getMealOption() == null) {

            throw new BusinessException(
                    "Meal option is required when response status is ACCEPTED.");
        }

        if (request.getResponseStatus()
                == MealResponseStatus.DECLINED
                && request.getMealOption() != null) {

            throw new BusinessException(
                    "Meal option must be empty when response status is DECLINED.");
        }

        if (request.getResponseStatus()
                == MealResponseStatus.DECLINED
                && request.getExtraRotiCount() > 0) {

            throw new BusinessException(
                    "Extra roti count must be zero when response status is DECLINED.");
        }

        mealResponseMapper.updateMealResponseFromRequest(
                request,
                mealResponse);

        mealResponse.setRespondedAt(
                LocalDateTime.now());

        MealResponse savedMealResponse =
                mealResponseRepository.save(mealResponse);

        dashboardWebSocketService.broadcastDashboard(
                menu.getMealSession());

        return mealResponseMapper.toResponse(
                savedMealResponse);
    }

    @Override
    public List<MealResponseResponse> getResponsesByMenu(
            Long menuId) {

        Menu menu =
                menuRepository.findById(menuId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Menu not found with ID: "
                                                + menuId));

        List<MealResponse> mealResponses =
                mealResponseRepository.findByMenu(menu);

        return mealResponseMapper.toResponseList(
                mealResponses);
    }

    @Override
    public MealResponseResponse getCustomerResponse(
            Long customerId,
            Long menuId) {

        // OWNER can access any customer.
        // CUSTOMER can access only their own response.
        customerSecurity.checkCustomerAccess(customerId);

        return mealResponseRepository
                .findByCustomerCustomerIdAndMenuMenuId(
                        customerId,
                        menuId)
                .map(mealResponseMapper::toResponse)
                .orElse(null);
    }
}