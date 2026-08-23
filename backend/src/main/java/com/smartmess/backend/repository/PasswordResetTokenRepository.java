package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.PasswordResetToken;
import com.smartmess.backend.enums.UserRole;

@Repository
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    List<PasswordResetToken> findByUserIdAndUserRoleAndUsedAtIsNull(
            Long userId,
            UserRole userRole
    );
}