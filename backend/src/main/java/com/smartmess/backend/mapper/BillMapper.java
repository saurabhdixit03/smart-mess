package com.smartmess.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.response.BillDetailResponse;
import com.smartmess.backend.dto.response.BillResponse;
import com.smartmess.backend.entity.Bill;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.fullName")
    BillResponse toResponse(Bill bill);

    List<BillResponse> toResponseList(
            List<Bill> bills
    );

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerName", source = "customer.fullName")

    /*
     * mealRecords will be populated
     * inside the Service layer.
     */
    @Mapping(target = "mealRecords", ignore = true)
    BillDetailResponse toDetailResponse(Bill bill);

}