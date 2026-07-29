package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public BillController(
            BillService billService) {

        this.billService = billService;

    }

    /*
     * Generate Bills
     */
    @PostMapping("/generate")
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
     */
    @GetMapping("/customer/{customerId}")
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
     * Bill Details
     */
    @GetMapping("/{billId}")
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
     */
    @GetMapping("/overview")
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

        return ResponseEntity.ok(
                response
        );

    }
}