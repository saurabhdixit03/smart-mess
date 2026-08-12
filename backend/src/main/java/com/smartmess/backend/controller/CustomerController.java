
package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.service.CustomerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Get a customer by ID.
     *
     * OWNER:
     * Can access any customer.
     *
     * CUSTOMER:
     * Can access only their own customer record.
     */
    @GetMapping("/{customerId}")
    @PreAuthorize(
            "hasRole('OWNER') or " +
            "(hasRole('CUSTOMER') and " +
            "#customerId == authentication.principal.userId)"
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.getCustomerById(customerId);

        ApiResponse<CustomerResponse> apiResponse =
                ApiResponse.success(
                        "Customer retrieved successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all customers.
     *
     * OWNER only.
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            HttpServletRequest request) {

        List<CustomerResponse> response =
                customerService.getAllCustomers();

        ApiResponse<List<CustomerResponse>> apiResponse =
                ApiResponse.success(
                        "Customers retrieved successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update customer remarks.
     *
     * OWNER only.
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest requestBody,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.updateCustomer(
                        customerId,
                        requestBody
                );

        ApiResponse<CustomerResponse> apiResponse =
                ApiResponse.success(
                        "Customer remarks updated successfully.",
                        request.getRequestURI(),
                        response
                );

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deactivate a customer.
     *
     * OWNER only.
     */
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        customerService.deleteCustomer(customerId);

        ApiResponse<Void> apiResponse =
                ApiResponse.success(
                        "Customer deactivated successfully.",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity.ok(apiResponse);
    }
}