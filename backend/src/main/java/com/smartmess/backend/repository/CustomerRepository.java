package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Authentication

    Optional<Customer> findByMobileNumber(String mobileNumber);

    Optional<Customer> findByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);


    // Customer Status

    List<Customer> findAllByStatus(CustomerStatus status);

    Optional<Customer> findByCustomerIdAndStatus(
            Long customerId,
            CustomerStatus status
    );


    // Live Dashboard

    long countByStatus(CustomerStatus status);


    // Billing Module

    List<Customer> findByStatus(
            CustomerStatus status
    );
}