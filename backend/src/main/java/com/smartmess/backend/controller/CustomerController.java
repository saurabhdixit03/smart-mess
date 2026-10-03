package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    public CustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }

    /**
     * Gets an active customer.
     *
     * Owners are restricted to their own mess.
     * Customers can access only their own record.
     */
    @GetMapping("/{customerId}")
    @PreAuthorize(
            "hasRole('OWNER') or "
                    + "(hasRole('CUSTOMER') and "
                    + "#customerId == authentication.principal.userId)"
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.getCustomerById(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer retrieved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /**
     * Includes active, inactive and pending accounts.
     * Returns only customers within the owner's mess.
     */
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            HttpServletRequest request) {

        List<CustomerResponse> response =
                customerService.getAllCustomers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customers retrieved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /**
     * Updates owner-managed customer remarks.
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

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer remarks updated successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /**
     * Approves a pending registration within the owner's mess.
     */
    @PatchMapping("/{customerId}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> approveCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.approveCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer approved successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /**
     * Rejects and permanently removes a pending registration.
     * Does not send an email.
     */
    @PatchMapping("/{customerId}/reject")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> rejectCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        customerService.rejectCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Registration rejected and removed.",
                        request.getRequestURI(),
                        null
                )
        );
    }

    /**
     * Reactivates an inactive account within the owner's mess.
     */
    @PatchMapping("/{customerId}/reactivate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> reactivateCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        CustomerResponse response =
                customerService.reactivateCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer reactivated successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }

    /**
     * Soft-deactivates an active customer.
     */
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        customerService.deleteCustomer(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer deactivated successfully.",
                        request.getRequestURI(),
                        null
                )
        );
    }
}