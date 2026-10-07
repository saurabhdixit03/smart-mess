package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    /*
     * Authentication: email and mobile remain globally unique.
     */
    Optional<Customer> findByMobileNumber(String mobileNumber);

    Optional<Customer> findByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    /*
     * Tenant-scoped customer operations.
     */
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

    List<Customer> findAllByMess_MessIdAndStatus(
            Long messId,
            CustomerStatus status
    );

    /*
     * Active customer lookup for meal collection.
     *
     * Includes customers regardless of response or collection status.
     * The service supplies an escaped LIKE pattern and limits results.
     */
    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.mess.messId = :messId
              AND c.status =
                  com.smartmess.backend.enums.CustomerStatus.ACTIVE
              AND (
                  LOWER(c.fullName) LIKE :searchPattern ESCAPE '!'
                  OR c.mobileNumber LIKE :searchPattern ESCAPE '!'
              )
            ORDER BY LOWER(c.fullName) ASC, c.customerId ASC
            """)
    List<Customer> searchActiveCustomersForCollection(
            @Param("messId") Long messId,
            @Param("searchPattern") String searchPattern,
            Pageable pageable
    );

    /*
     * Serializes operations affecting one customer's participation.
     *
     * Must be called inside a write transaction.
     * Status is checked by the service after acquiring the lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.customerId = :customerId
              AND c.mess.messId = :messId
            """)
    Optional<Customer> findByCustomerIdAndMessIdForUpdate(
            @Param("customerId") Long customerId,
            @Param("messId") Long messId
    );
}