package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.smartmess.backend.enums.BillStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillDetailResponse {

    private Long billId;

    private Long messId;

    private String messName;

    private Long customerId;

    private String customerName;

    private String customerMobileNumber;

    private String customerEmail;

    private Integer billingMonth;

    private Integer billingYear;

    private Integer mealRecordCount;

    private BigDecimal totalAmount;

    private BillStatus billStatus;

    private LocalDateTime generatedAt;

    /*
     * Original recorded meal charges attached to this bill.
     * Populated by the service.
     */
    private List<MealRecordResponse> mealRecords;

    /*
     * Recorded payment and its settled gateway metadata.
     * Null when no payment has been recorded.
     * Populated by the service.
     */
    private BillPaymentReceiptResponse payment;
}