package com.smartmess.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.smartmess.backend.enums.UserRole;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String mobileNumber;
    private final String password;
    private final UserRole role;

    public CustomUserDetails(
            Long userId,
            String mobileNumber,
            String password,
            UserRole role) {

        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return mobileNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}