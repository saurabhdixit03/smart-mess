package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PaymentOverviewResponse {

    private Long unpaidBillCount;

    private Long paidBillCount;

    private BigDecimal totalCollectedAmount;

    private BigDecimal totalOutstandingAmount;

    private List<BillResponse> unpaidBills;

    private List<BillResponse> paidBills;

    public Long getUnpaidBillCount() {
        return unpaidBillCount;
    }

    public void setUnpaidBillCount(Long unpaidBillCount) {
        this.unpaidBillCount = unpaidBillCount;
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

    public void setTotalCollectedAmount(
            BigDecimal totalCollectedAmount) {

        this.totalCollectedAmount = totalCollectedAmount;
    }

    public BigDecimal getTotalOutstandingAmount() {
        return totalOutstandingAmount;
    }

    public void setTotalOutstandingAmount(
            BigDecimal totalOutstandingAmount) {

        this.totalOutstandingAmount = totalOutstandingAmount;
    }

    public List<BillResponse> getUnpaidBills() {
        return unpaidBills;
    }

    public void setUnpaidBills(List<BillResponse> unpaidBills) {
        this.unpaidBills = unpaidBills;
    }

    public List<BillResponse> getPaidBills() {
        return paidBills;
    }

    public void setPaidBills(List<BillResponse> paidBills) {
        this.paidBills = paidBills;
    }
}