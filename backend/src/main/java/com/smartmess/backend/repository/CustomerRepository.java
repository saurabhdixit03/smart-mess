package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByMobileNumber(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);
    
    List<Customer> findAllByStatus(CustomerStatus status);

    Optional<Customer> findByCustomerIdAndStatus(
            Long customerId,
            CustomerStatus status);
    
    // for live dashboard
    long countByStatus(CustomerStatus status);
    
    // for billing module
    
    List<Customer> findByStatus(
            CustomerStatus status
    );

}