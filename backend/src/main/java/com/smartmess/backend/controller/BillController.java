
package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.GenerateBillRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.BillDetailResponse;
import com.smartmess.backend.dto.response.BillResponse;
import com.smartmess.backend.dto.response.BillingOverviewResponse;
import com.smartmess.backend.service.BillService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    /*
     * Generate Bills
     *
     * Owner only.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> generateBills(

            @Valid
            @RequestBody
            GenerateBillRequest request,

            HttpServletRequest httpRequest) {

        List<BillResponse> bills =
                billService.generateBills(request);

        ApiResponse<List<BillResponse>> response =
                ApiResponse.success(
                        "Bills generated successfully.",
                        httpRequest.getRequestURI(),
                        bills
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
     * Customer Bill History
     *
     * Owner only.
     *
     * Owner can request bills for any customer.
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getCustomerBills(

            @PathVariable Long customerId,

            HttpServletRequest request) {

        List<BillResponse> bills =
                billService.getCustomerBills(customerId);

        ApiResponse<List<BillResponse>> response =
                ApiResponse.success(
                        "Customer bills retrieved successfully.",
                        request.getRequestURI(),
                        bills
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Authenticated Customer Bill History
     *
     * Customer only.
     *
     * Customer ID comes from the JWT.
     * The client does NOT provide a customer ID.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getMyBills(

            HttpServletRequest request) {

        List<BillResponse> bills =
                billService.getMyBills();

        ApiResponse<List<BillResponse>> response =
                ApiResponse.success(
                        "Your bills retrieved successfully.",
                        request.getRequestURI(),
                        bills
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Bill Details
     *
     * Owner + Customer.
     *
     * Owner can view any bill.
     *
     * Customer can view only their own bill.
     *
     * Ownership validation is handled inside BillServiceImpl.
     */
    @GetMapping("/{billId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BillDetailResponse>> getBillDetails(

            @PathVariable Long billId,

            HttpServletRequest request) {

        BillDetailResponse bill =
                billService.getBillDetails(billId);

        ApiResponse<BillDetailResponse> response =
                ApiResponse.success(
                        "Bill details retrieved successfully.",
                        request.getRequestURI(),
                        bill
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Billing Overview
     *
     * Owner only.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BillingOverviewResponse>> getBillingOverview(

            @RequestParam Integer billingMonth,

            @RequestParam Integer billingYear,

            HttpServletRequest request) {

        BillingOverviewResponse overview =
                billService.getBillingOverview(
                        billingMonth,
                        billingYear
                );

        ApiResponse<BillingOverviewResponse> response =
                ApiResponse.success(
                        "Billing overview retrieved successfully.",
                        request.getRequestURI(),
                        overview
                );

        return ResponseEntity.ok(response);
    }
}
