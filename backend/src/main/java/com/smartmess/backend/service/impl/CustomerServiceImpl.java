package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateCustomerRequest;
import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.CustomerMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.service.CustomerService;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.exception.ResourceNotFoundException;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerMapper customerMapper) {

        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        // Business Rule: Mobile number must be unique
        if (customerRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new BusinessException(
                    "A customer with this mobile number already exists."
            );
        }

        // Convert Request DTO to Entity
        Customer customer = customerMapper.toEntity(request);

        // Save Customer
        Customer savedCustomer = customerRepository.save(customer);

        // Convert Entity to Response DTO
        return customerMapper.toResponse(savedCustomer);
    }
    
    
    @Override
    public CustomerResponse getCustomerById(Long customerId) {

        Customer customer = customerRepository.findByCustomerIdAndStatus(
                customerId,
                CustomerStatus.ACTIVE
        )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found."));

        return customerMapper.toResponse(customer);
    }
    
    @Override
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAllByStatus(CustomerStatus.ACTIVE)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId,
                                           UpdateCustomerRequest request) {

        // Find existing customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found."));

        // Check if mobile number has changed
        if (!customer.getMobileNumber().equals(request.mobileNumber())) {

            // Check if new mobile number already exists
            if (customerRepository.existsByMobileNumber(request.mobileNumber())) {
                throw new BusinessException(
                        "A customer with this mobile number already exists."
                );
            }
        }

        // Update entity using MapStruct
        customerMapper.updateCustomerFromRequest(request, customer);

        // Save updated entity
        Customer updatedCustomer = customerRepository.save(customer);

        // Return response DTO
        return customerMapper.toResponse(updatedCustomer);
    }

    
    @Override
    public void deleteCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found."));

        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessException("Customer is already inactive.");
        }

        customer.setStatus(CustomerStatus.INACTIVE);

        customerRepository.save(customer);
    }
}