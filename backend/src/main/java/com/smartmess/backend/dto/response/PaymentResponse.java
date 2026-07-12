package com.smartmess.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartmess.backend.enums.PaymentMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long paymentId;

    private Long billId;

    private Long customerId;

    private String customerName;

    private BigDecimal paymentAmount;

    private PaymentMode paymentMode;

    private LocalDateTime paidAt;

}