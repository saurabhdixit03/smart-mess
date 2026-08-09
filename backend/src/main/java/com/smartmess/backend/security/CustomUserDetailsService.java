package com.smartmess.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MessOwnerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MessOwnerRepository messOwnerRepository;
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(
            MessOwnerRepository messOwnerRepository,
            CustomerRepository customerRepository) {

        this.messOwnerRepository = messOwnerRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Required by Spring Security.
     *
     * Authentication is handled through JWT, so this method
     * is not used directly by our JWT authentication flow.
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        throw new UnsupportedOperationException(
                "Use loadUserByMobileNumber() instead."
        );
    }

    /**
     * Loads the authenticated user using the mobile number
     * and role extracted from the JWT.
     */
    public UserDetails loadUserByMobileNumber(
            String mobileNumber,
            UserRole role) {

        switch (role) {

            case OWNER:

                MessOwner owner = messOwnerRepository
                        .findByMobileNumber(mobileNumber)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Owner not found."
                                ));

                return new CustomUserDetails(
                        owner.getMessOwnerId(),
                        owner.getMobileNumber(),
                        owner.getPassword(),
                        UserRole.OWNER
                );

            case CUSTOMER:

                Customer customer = customerRepository
                        .findByMobileNumber(mobileNumber)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Customer not found."
                                ));

                return new CustomUserDetails(
                        customer.getCustomerId(),
                        customer.getMobileNumber(),
                        customer.getPassword(),
                        UserRole.CUSTOMER
                );

            default:

                throw new UsernameNotFoundException(
                        "Unsupported user role."
                );
        }
    }
}