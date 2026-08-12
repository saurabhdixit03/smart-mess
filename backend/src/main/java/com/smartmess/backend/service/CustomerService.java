
package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;

public interface CustomerService {

    CustomerResponse getCustomerById(Long customerId);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(
            Long customerId,
            UpdateCustomerRequest request
    );

    void deleteCustomer(Long customerId);
}
