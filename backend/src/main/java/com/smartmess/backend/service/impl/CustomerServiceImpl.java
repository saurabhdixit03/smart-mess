package com.smartmess.backend.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartmess.backend.dto.request.UpdateCustomerRequest;
import com.smartmess.backend.dto.response.CustomerResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.CustomerMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.CustomerService;
import com.smartmess.backend.service.EmailService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log =
            LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerSecurity customerSecurity;
    private final EmailService emailService;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            CustomerSecurity customerSecurity,
            EmailService emailService) {

        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.customerSecurity = customerSecurity;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerResponse getCustomerById(Long customerId) {

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                customerRepository
                        .findByCustomerIdAndMess_MessIdAndStatus(
                                customerId,
                                messId,
                                CustomerStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found."
                                ));

        customerSecurity.checkCustomerAccess(customerId);

        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CustomerResponse> getAllCustomers() {

        Long messId =
                customerSecurity.getCurrentMessId();

        return customerRepository
                .findAllByMess_MessId(messId)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public CustomerResponse updateCustomer(
            Long customerId,
            UpdateCustomerRequest request) {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        customerMapper.updateCustomerFromRequest(
                request,
                customer
        );

        Customer updatedCustomer =
                customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }

    /*
     * Approves a pending registration within the owner's mess.
     *
     * Inactive customers must use reactivation.
     */
    @Transactional
    @Override
    public CustomerResponse approveCustomer(Long customerId) {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        if (customer.getStatus() != CustomerStatus.PENDING) {

            throw new BusinessException(
                    "Only pending customer registrations can be approved."
            );
        }

        customer.setStatus(CustomerStatus.ACTIVE);

        Customer approvedCustomer =
                customerRepository.save(customer);

        scheduleApprovalEmail(approvedCustomer);

        return customerMapper.toResponse(approvedCustomer);
    }

    /*
     * Permanently removes an unapproved registration.
     *
     * Only pending accounts within the owner's mess qualify.
     * Active and inactive customer accounts cannot be deleted here.
     * No rejection email is sent.
     */
    @Transactional
    @Override
    public void rejectCustomer(Long customerId) {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        if (customer.getStatus() != CustomerStatus.PENDING) {

            throw new BusinessException(
                    "Only pending customer registrations can be rejected."
            );
        }

        customerRepository.delete(customer);
    }

    /*
     * Restores an inactive account within the owner's mess.
     *
     * Customer identity, password, joining date and historical
     * records are preserved. Pending registrations require approval.
     */
    @Transactional
    @Override
    public CustomerResponse reactivateCustomer(Long customerId) {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        if (customer.getStatus() != CustomerStatus.INACTIVE) {

            throw new BusinessException(
                    "Only inactive customers can be reactivated."
            );
        }

        customer.setStatus(CustomerStatus.ACTIVE);

        Customer reactivatedCustomer =
                customerRepository.save(customer);

        return customerMapper.toResponse(reactivatedCustomer);
    }

    /*
     * Soft-deactivates an active customer.
     *
     * Pending registrations must use approve or reject.
     * Approved customer history is retained.
     */
    @Transactional
    @Override
    public void deleteCustomer(Long customerId) {

        requireOwner();

        Long messId =
                customerSecurity.getCurrentMessId();

        Customer customer =
                findCustomer(customerId, messId);

        customerSecurity.checkCustomerAccess(customerId);

        if (customer.getStatus() == CustomerStatus.INACTIVE) {

            throw new BusinessException(
                    "Customer is already inactive."
            );
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {

            throw new BusinessException(
                    "Only active customers can be deactivated."
            );
        }

        customer.setStatus(CustomerStatus.INACTIVE);

        customerRepository.save(customer);
    }

    /*
     * Capture message data while the entity is attached.
     *
     * Send only after approval has committed successfully.
     * Email failure must not undo approval or fail its HTTP response.
     */
    private void scheduleApprovalEmail(Customer customer) {

        Long customerId = customer.getCustomerId();
        Long messId = customer.getMess().getMessId();

        String recipientEmail = customer.getEmail();
        String customerName = customer.getFullName();
        String messName = customer.getMess().getMessName();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        try {

                            emailService.sendCustomerApprovalEmail(
                                    recipientEmail,
                                    customerName,
                                    messName
                            );

                            log.info(
                                    "Customer approval email sent. Customer ID: {}, Mess ID: {}.",
                                    customerId,
                                    messId
                            );

                        } catch (RuntimeException exception) {

                            log.error(
                                    "Customer approved, but approval email could not be sent. "
                                            + "Customer ID: {}, Mess ID: {}.",
                                    customerId,
                                    messId,
                                    exception
                            );
                        }
                    }
                }
        );
    }

    /*
     * Resolves a customer only within the authenticated mess.
     */
    private Customer findCustomer(
            Long customerId,
            Long messId) {

        return customerRepository
                .findByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found."
                        ));
    }

    private void requireOwner() {

        if (customerSecurity.getCurrentUserRole()
                != UserRole.OWNER) {

            throw new AccessDeniedException(
                    "Only mess owners can manage customer accounts."
            );
        }
    }
}