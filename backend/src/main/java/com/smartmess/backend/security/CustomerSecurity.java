package com.smartmess.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.smartmess.backend.enums.UserRole;

@Component
public class CustomerSecurity {

    /**
     * Returns the currently authenticated user's ID.
     */
    public Long getCurrentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof CustomUserDetails userDetails)) {

            throw new AccessDeniedException(
                    "Authenticated user not found."
            );
        }

        return userDetails.getUserId();
    }

    /**
     * Returns the currently authenticated user's role.
     */
    public UserRole getCurrentUserRole() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof CustomUserDetails userDetails)) {

            throw new AccessDeniedException(
                    "Authenticated user not found."
            );
        }

        return userDetails.getRole();
    }

    /**
     * Allows OWNER access to any customer.
     *
     * CUSTOMER can access only their own customer ID.
     */
    public void checkCustomerAccess(Long customerId) {

        UserRole role = getCurrentUserRole();

        if (role == UserRole.OWNER) {
            return;
        }

        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(customerId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access this customer."
            );
        }
    }
}