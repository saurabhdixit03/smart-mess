package com.smartmess.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MessOwnerStatus;
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
     * The JWT authentication flow uses loadUserByEmail().
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        throw new UnsupportedOperationException(
                "Use loadUserByEmail() instead."
        );
    }

    /**
     * Loads tenant ownership and current account status
     * from the database.
     *
     * HTTP JWT authentication and WebSocket CONNECT
     * both use this account lookup.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(
            String email,
            UserRole role) {

        if (role == UserRole.OWNER) {

            MessOwner owner =
                    messOwnerRepository
                            .findByEmail(email)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Owner not found."
                                    ));

            if (owner.getStatus() != MessOwnerStatus.ACTIVE) {

                throw new UsernameNotFoundException(
                        "Owner account is not active."
                );
            }

            if (owner.getMess() == null
                    || owner.getMess().getMessId() == null) {

                throw new UsernameNotFoundException(
                        "Owner is not linked to a mess."
                );
            }

            return new CustomUserDetails(
                    owner.getMessOwnerId(),
                    owner.getEmail(),
                    owner.getPassword(),
                    UserRole.OWNER,
                    owner.getMess().getMessId()
            );
        }

        if (role == UserRole.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByEmail(email)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Customer not found."
                                    ));

            if (customer.getStatus() != CustomerStatus.ACTIVE) {

                throw new UsernameNotFoundException(
                        "Customer account is not active."
                );
            }

            if (customer.getMess() == null
                    || customer.getMess().getMessId() == null) {

                throw new UsernameNotFoundException(
                        "Customer is not linked to a mess."
                );
            }

            return new CustomUserDetails(
                    customer.getCustomerId(),
                    customer.getEmail(),
                    customer.getPassword(),
                    UserRole.CUSTOMER,
                    customer.getMess().getMessId()
            );
        }

        throw new UsernameNotFoundException(
                "Unsupported user role."
        );
    }
}