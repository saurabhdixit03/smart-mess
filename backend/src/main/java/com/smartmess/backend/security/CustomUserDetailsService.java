package com.smartmess.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.repository.MessOwnerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MessOwnerRepository messOwnerRepository;

    public CustomUserDetailsService(
            MessOwnerRepository messOwnerRepository) {

        this.messOwnerRepository = messOwnerRepository;
    }

    /**
     * Required by Spring Security.
     * We don't use this method directly because our JWT contains
     * both mobile number and role.
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        throw new UnsupportedOperationException(
                "Use loadUserByMobileNumber() instead."
        );
    }

    /**
     * Used by JwtAuthenticationFilter.
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
                throw new UnsupportedOperationException(
                        "Customer authentication not implemented yet."
                );

            default:
                throw new UsernameNotFoundException(
                        "Unsupported user role."
                );
        }
    }
}