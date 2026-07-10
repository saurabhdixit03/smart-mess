package com.smartmess.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.BillStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "bills",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "customer_id",
                                "billing_month",
                                "billing_year"
                        }
                )
        }
)
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Column(
            name = "billing_month",
            nullable = false
    )
    private Integer billingMonth;

    @Column(
            name = "billing_year",
            nullable = false
    )
    private Integer billingYear;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Integer mealRecordCount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private BillStatus billStatus;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        generatedAt = now;

    }

    public Bill() {
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getMealRecordCount() {
        return mealRecordCount;
    }

    public void setMealRecordCount(Integer mealRecordCount) {
        this.mealRecordCount = mealRecordCount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}