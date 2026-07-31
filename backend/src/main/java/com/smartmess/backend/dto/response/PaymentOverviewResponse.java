package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PaymentOverviewResponse {

    private Long unpaidBillCount;

    private Long pendingRequestCount;

    private Long paidBillCount;

    private BigDecimal totalCollectedAmount;

    private List<BillResponse> unpaidBills;

    private List<PendingPaymentResponse> pendingPayments;

    public Long getUnpaidBillCount() {
        return unpaidBillCount;
    }

    public void setUnpaidBillCount(Long unpaidBillCount) {
        this.unpaidBillCount = unpaidBillCount;
    }

    public Long getPendingRequestCount() {
        return pendingRequestCount;
    }

    public void setPendingRequestCount(Long pendingRequestCount) {
        this.pendingRequestCount = pendingRequestCount;
    }

    public Long getPaidBillCount() {
        return paidBillCount;
    }

    public void setPaidBillCount(Long paidBillCount) {
        this.paidBillCount = paidBillCount;
    }

    public BigDecimal getTotalCollectedAmount() {
        return totalCollectedAmount;
    }

    public void setTotalCollectedAmount(BigDecimal totalCollectedAmount) {
        this.totalCollectedAmount = totalCollectedAmount;
    }

    public List<BillResponse> getUnpaidBills() {
        return unpaidBills;
    }

    public void setUnpaidBills(List<BillResponse> unpaidBills) {
        this.unpaidBills = unpaidBills;
    }

    public List<PendingPaymentResponse> getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(
            List<PendingPaymentResponse> pendingPayments) {
        this.pendingPayments = pendingPayments;
    }
}