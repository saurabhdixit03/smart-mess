package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CreateCustomerRequest;
import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * TEMPORARY:
     * Maps the existing Owner Portal customer creation request.
     *
     * This method will be removed later after the
     * Owner-side customer creation functionality is removed.
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

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
     * Updates an existing Customer entity.
     *
     * Owner-managed remarks remain supported.
     * Customer identity and authentication data are protected.
     */
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "mobileNumber", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "joiningDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateCustomerFromRequest(
            UpdateCustomerRequest request,
            @MappingTarget Customer customer
    );
}