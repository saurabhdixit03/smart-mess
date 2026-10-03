package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Authentication: email and mobile remain globally unique.

    Optional<Customer> findByMobileNumber(String mobileNumber);

    Optional<Customer> findByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    // Tenant-scoped customer operations.

    List<Customer> findAllByMess_MessId(Long messId);

    Optional<Customer> findByCustomerIdAndMess_MessId(
            Long customerId,
            Long messId
    );

    Optional<Customer> findByCustomerIdAndMess_MessIdAndStatus(
            Long customerId,
            Long messId,
            CustomerStatus status
    );

    boolean existsByCustomerIdAndMess_MessId(
            Long customerId,
            Long messId
    );

    /*
     * Tenant-scoped customer status lookup.
     *
     * Used by notifications, the live dashboard,
     * billing, and demo seeding to select customers
     * within one mess.
     */
    List<Customer> findAllByMess_MessIdAndStatus(
            Long messId,
            CustomerStatus status
    );
}