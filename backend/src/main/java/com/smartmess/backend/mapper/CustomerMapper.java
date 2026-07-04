package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CreateCustomerRequest;
import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCustomerFromRequest(UpdateCustomerRequest request,
                                   @MappingTarget Customer customer);

}