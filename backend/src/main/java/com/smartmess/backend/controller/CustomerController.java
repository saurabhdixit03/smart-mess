package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateCustomerRequest;
import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.service.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@Validated
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            HttpServletRequest httpRequest) {

        CustomerResponse response = customerService.createCustomer(request);

        ApiResponse<CustomerResponse> apiResponse = ApiResponse.success(
                "Customer created successfully.",
                httpRequest.getRequestURI(),
                response
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
    
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        CustomerResponse response = customerService.getCustomerById(customerId);

        ApiResponse<CustomerResponse> apiResponse = ApiResponse.success(
                "Customer retrieved successfully.",
                request.getRequestURI(),
                response
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            HttpServletRequest request) {

        List<CustomerResponse> response = customerService.getAllCustomers();

        ApiResponse<List<CustomerResponse>> apiResponse = ApiResponse.success(
                "Customers retrieved successfully.",
                request.getRequestURI(),
                response
        );

        return ResponseEntity.ok(apiResponse);
    }
    
    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest requestBody,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.updateCustomer(customerId, requestBody);

        ApiResponse<CustomerResponse> apiResponse =
                ApiResponse.success(
                        "Customer updated successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }
    
    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        customerService.deleteCustomer(customerId);

        ApiResponse<Void> apiResponse = ApiResponse.success(
                "Customer deactivated successfully.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.ok(apiResponse);
    }
}