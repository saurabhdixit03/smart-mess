package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.response.PaymentResponse;
import com.smartmess.backend.entity.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "billId", source = "bill.billId")

    @Mapping(target = "customerId", source = "bill.customer.customerId")

    @Mapping(target = "customerName", source = "bill.customer.fullName")

    PaymentResponse toResponse(
            Payment payment
    );

}