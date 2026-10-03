package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Maps customer self-registration fields.
     *
     * The authentication service assigns the mess,
     * encodes the password and sets the joining date.
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRegistrationRequest request);

    /**
     * Maps Customer entity to the existing response DTO.
     */
    CustomerResponse toResponse(Customer customer);

    /**
     * Updates only owner-managed remarks.
     *
     * Customer identity, tenant ownership, authentication data,
     * status and joining date remain protected.
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "mobileNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCustomerFromRequest(
            UpdateCustomerRequest request,
            @MappingTarget Customer customer
    );
}