package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Manual generation from unbilled collected meals.
     * Supports one customer or all eligible customers.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> generateBills(
            @Valid @RequestBody GenerateBillRequest request,
            HttpServletRequest httpRequest) {

        List<BillResponse> bills =
                billService.generateBills(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Bills generated successfully.",
                                httpRequest.getRequestURI(),
                                bills
                        )
                );
    }

    /*
     * Owner access is restricted to customers in their own mess.
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getCustomerBills(
            @PathVariable Long customerId,
            HttpServletRequest request) {

        List<BillResponse> bills =
                billService.getCustomerBills(customerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer bills retrieved successfully.",
                        request.getRequestURI(),
                        bills
                )
        );
    }

    /*
     * Customer identity comes from the authenticated session.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getMyBills(
            HttpServletRequest request) {

        List<BillResponse> bills =
                billService.getMyBills();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Your bills retrieved successfully.",
                        request.getRequestURI(),
                        bills
                )
        );
    }

    /*
     * Shared bill document, including recorded payment details.
     * The service validates tenant and customer ownership.
     */
    @GetMapping("/{billId}")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<BillDetailResponse>> getBillDetails(
            @PathVariable Long billId,
            HttpServletRequest request) {

        BillDetailResponse bill =
                billService.getBillDetails(billId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Bill details retrieved successfully.",
                        request.getRequestURI(),
                        bill
                )
        );
    }

    /*
     * Both parameters omitted: all billing periods.
     * Both supplied: one billing period.
     * The service rejects incomplete or invalid periods.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<BillingOverviewResponse>> getBillingOverview(
            @RequestParam(required = false) Integer billingMonth,
            @RequestParam(required = false) Integer billingYear,
            HttpServletRequest request) {

        BillingOverviewResponse overview =
                billService.getBillingOverview(
                        billingMonth,
                        billingYear
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Billing overview retrieved successfully.",
                        request.getRequestURI(),
                        overview
                )
        );
    }
}