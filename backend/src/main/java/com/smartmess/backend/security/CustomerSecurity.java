package com.smartmess.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.repository.CustomerRepository;

@Component
public class CustomerSecurity {

    private final CustomerRepository customerRepository;

    public CustomerSecurity(
            CustomerRepository customerRepository) {

        this.customerRepository = customerRepository;
    }

    /**
     * Returns the currently authenticated user's ID.
     */
    public Long getCurrentUserId() {

        return getAuthenticatedUser().getUserId();
    }

    /**
     * Returns the currently authenticated user's role.
     */
    public UserRole getCurrentUserRole() {

        return getAuthenticatedUser().getRole();
    }

    /**
     * Returns the authenticated account's mess ID.
     */
    public Long getCurrentMessId() {

        Long messId =
                getAuthenticatedUser().getMessId();

        if (messId == null) {

            throw new AccessDeniedException(
                    "Authenticated account is not linked to a mess."
            );
        }

        return messId;
    }

    /**
     * OWNER can access customers only within their own mess.
     *
     * CUSTOMER can access only their own account within that mess.
     */
    public void checkCustomerAccess(Long customerId) {

        CustomUserDetails userDetails =
                getAuthenticatedUser();

        UserRole role =
                userDetails.getRole();

        if (role != UserRole.OWNER
                && role != UserRole.CUSTOMER) {

            throw customerAccessDenied();
        }

        if (role == UserRole.CUSTOMER
                && !userDetails.getUserId().equals(customerId)) {

            throw customerAccessDenied();
        }

        Long messId =
                getCurrentMessId();

        if (customerId == null
                || !customerRepository.existsByCustomerIdAndMess_MessId(
                        customerId,
                        messId
                )) {

            throw customerAccessDenied();
        }
    }

    private CustomUserDetails getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof CustomUserDetails userDetails)) {

            throw new AccessDeniedException(
                    "Authenticated user not found."
            );
        }

        return userDetails;
    }

    private AccessDeniedException customerAccessDenied() {

        return new AccessDeniedException(
                "You do not have permission to access this customer."
        );
    }
}