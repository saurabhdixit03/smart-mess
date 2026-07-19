package com.smartmess.backend.dto.response;

import java.math.BigDecimal;

public record UpiPaymentResponse(

        String upiUrl,
        
        String upiId,

        String receiverName,

        BigDecimal amount,

        Long billId

) {
}