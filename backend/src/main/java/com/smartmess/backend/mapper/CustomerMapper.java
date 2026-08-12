
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
     * Maps Customer self-registration request to Customer entity.
     *
     * Password is handled separately by the authentication service
     * so that it can be encoded before persistence.
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "password", ignore = true)
    Customer toEntity(CustomerRegistrationRequest request);

    /**
     * Maps Customer entity to CustomerResponse DTO.
     */
    CustomerResponse toResponse(Customer customer);

    /**
     * Updates only the remarks managed from the Owner Portal.
     *
     * Customer identity, authentication data,
     * status, and joining date remain protected.
     */
    @Mapping(target = "customerId", ignore = true)
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