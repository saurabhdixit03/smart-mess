package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.GenerateBillRequest;
import com.smartmess.backend.dto.response.BillDetailResponse;
import com.smartmess.backend.dto.response.BillResponse;
import com.smartmess.backend.dto.response.BillingOverviewResponse;

public interface BillService {

    /*
     * Generate bills for a billing period.
     */
    List<BillResponse> generateBills(
            GenerateBillRequest request
    );

    /*
     * View all bills of a customer.
     */
    List<BillResponse> getCustomerBills(
            Long customerId
    );

    /*
     * View complete bill details.
     */
    BillDetailResponse getBillDetails(
            Long billId
    );
    
    /*
     * View all bills for a billing period.
     */
    BillingOverviewResponse getBillingOverview(
            Integer billingMonth,
            Integer billingYear
    );
    
    /*
     * exists specifically for the authenticated customer.
     */
    List<BillResponse> getMyBills();
    
     

}