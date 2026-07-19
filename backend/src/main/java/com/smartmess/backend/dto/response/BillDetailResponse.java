package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.smartmess.backend.enums.BillStatus;

public class BillDetailResponse {

    private Long billId;

    private Long customerId;

    private String customerName;

    private Integer billingMonth;

    private Integer billingYear;

    private Integer mealRecordCount;

    private BigDecimal totalAmount;

    private BillStatus billStatus;

    private LocalDateTime generatedAt;

    /*
     * Meal Records used to generate this bill.
     */
    private List<MealRecordResponse> mealRecords;

    public BillDetailResponse() {
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getBillingMonth() {
        return billingMonth;
    }

    public void setBillingMonth(Integer billingMonth) {
        this.billingMonth = billingMonth;
    }

    public Integer getBillingYear() {
        return billingYear;
    }

    public void setBillingYear(Integer billingYear) {
        this.billingYear = billingYear;
    }

    public Integer getMealRecordCount() {
        return mealRecordCount;
    }

    public void setMealRecordCount(Integer mealRecordCount) {
        this.mealRecordCount = mealRecordCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BillStatus getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(BillStatus billStatus) {
        this.billStatus = billStatus;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<MealRecordResponse> getMealRecords() {
        return mealRecords;
    }

    public void setMealRecords(List<MealRecordResponse> mealRecords) {
        this.mealRecords = mealRecords;
    }

}