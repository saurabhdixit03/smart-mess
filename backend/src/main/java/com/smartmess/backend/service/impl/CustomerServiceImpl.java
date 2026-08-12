
package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.CustomerMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper) {

        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {

        Customer customer =
                customerRepository.findByCustomerIdAndStatus(
                        customerId,
                        CustomerStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found."
                        ));

        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(
            Long customerId,
            UpdateCustomerRequest request) {

        Customer customer =
                customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found."
                        ));

        customerMapper.updateCustomerFromRequest(
                request,
                customer
        );

        Customer updatedCustomer =
                customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {

        Customer customer =
                customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found."
                        ));

        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessException(
                    "Customer is already inactive."
            );
        }

        customer.setStatus(CustomerStatus.INACTIVE);

        customerRepository.save(customer);
    }
}

