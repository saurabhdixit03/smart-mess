package com.smartmess.backend.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.request.ForgotPasswordRequest;
import com.smartmess.backend.dto.request.ResetPasswordRequest;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.entity.PasswordResetToken;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MessOwnerRepository;
import com.smartmess.backend.repository.PasswordResetTokenRepository;
import com.smartmess.backend.service.EmailService;
import com.smartmess.backend.service.PasswordResetService;

@Service
public class PasswordResetServiceImpl
        implements PasswordResetService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final MessOwnerRepository
            messOwnerRepository;

    private final CustomerRepository
            customerRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final EmailService
            emailService;

    private final Clock clock;

    @Value("${app.password-reset.expiration-minutes:30}")
    private long tokenExpirationMinutes;

    public PasswordResetServiceImpl(
            PasswordResetTokenRepository passwordResetTokenRepository,
            MessOwnerRepository messOwnerRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            Clock clock) {

        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.messOwnerRepository =
                messOwnerRepository;

        this.customerRepository =
                customerRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.emailService =
                emailService;

        this.clock =
                clock;
    }

    @Override
    @Transactional
    public void requestPasswordReset(
            ForgotPasswordRequest request,
            UserRole userRole) {

        Long userId;
        String email;

        if (userRole == UserRole.OWNER) {

            MessOwner owner =
                    messOwnerRepository
                            .findByEmail(
                                    request.email()
                            )
                            .orElse(null);

            /*
             * Do not reveal whether the account exists.
             */
            if (owner == null) {
                return;
            }

            userId = owner.getMessOwnerId();
            email = owner.getEmail();

        } else {

            Customer customer =
                    customerRepository
                            .findByEmail(
                                    request.email()
                            )
                            .orElse(null);

            /*
             * Do not reveal whether the account exists.
             */
            if (customer == null) {
                return;
            }

            userId = customer.getCustomerId();
            email = customer.getEmail();
        }

        /*
         * Defensive check during migration.
         *
         * Customer email is now required,
         * but this avoids exposing account state
         * if legacy data ever exists.
         */
        if (email == null || email.isBlank()) {
            return;
        }

        invalidateExistingTokens(
                userId,
                userRole
        );

        String rawToken =
                generateSecureToken();

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setTokenHash(
                tokenHash
        );

        resetToken.setUserId(
                userId
        );

        resetToken.setUserRole(
                userRole
        );

        resetToken.setExpiresAt(
                LocalDateTime.now(clock)
                        .plusMinutes(
                                tokenExpirationMinutes
                        )
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        /*
         * Raw token is never stored.
         * It exists only in the reset email.
         */
        emailService.sendPasswordResetEmail(
                email,
                rawToken,
                userRole
        );
    }

    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request) {

        if (!request.newPassword()
                .equals(request.confirmPassword())) {

            throw new BusinessException(
                    "Passwords do not match."
            );
        }

        String tokenHash =
                hashToken(
                        request.token()
                );

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid or expired password reset token."
                                )
                        );

        if (resetToken.getUsedAt() != null) {

            throw new BusinessException(
                    "Invalid or expired password reset token."
            );
        }

        if (resetToken
                .getExpiresAt()
                .isBefore(
                        LocalDateTime.now(clock)
                )) {

            throw new BusinessException(
                    "Invalid or expired password reset token."
            );
        }

        if (resetToken.getUserRole()
                == UserRole.OWNER) {

            resetOwnerPassword(
                    resetToken.getUserId(),
                    request.newPassword()
            );

        } else {

            resetCustomerPassword(
                    resetToken.getUserId(),
                    request.newPassword()
            );
        }

        /*
         * Invalidate every outstanding reset link
         * for this account after successful reset.
         */
        invalidateExistingTokens(
                resetToken.getUserId(),
                resetToken.getUserRole()
        );
    }

    private void resetOwnerPassword(
            Long ownerId,
            String newPassword) {

        MessOwner owner =
                messOwnerRepository
                        .findById(ownerId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid or expired password reset token."
                                )
                        );

        validateNewPassword(
                newPassword,
                owner.getPassword()
        );

        owner.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        messOwnerRepository.save(owner);
    }

    private void resetCustomerPassword(
            Long customerId,
            String newPassword) {

        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid or expired password reset token."
                                )
                        );

        validateNewPassword(
                newPassword,
                customer.getPassword()
        );

        customer.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        customerRepository.save(customer);
    }

    private void validateNewPassword(
            String newPassword,
            String currentPasswordHash) {

        if (passwordEncoder.matches(
                newPassword,
                currentPasswordHash
        )) {

            throw new BusinessException(
                    "New password must be different from your current password."
            );
        }
    }

    private void invalidateExistingTokens(
            Long userId,
            UserRole userRole) {

        List<PasswordResetToken> activeTokens =
                passwordResetTokenRepository
                        .findByUserIdAndUserRoleAndUsedAtIsNull(
                                userId,
                                userRole
                        );

        if (activeTokens.isEmpty()) {
            return;
        }

        LocalDateTime now =
                LocalDateTime.now(clock);

        activeTokens.forEach(
                token ->
                        token.setUsedAt(now)
        );

        passwordResetTokenRepository.saveAll(
                activeTokens
        );
    }

    private String generateSecureToken() {

        byte[] randomBytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(
                randomBytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        randomBytes
                );
    }

    private String hashToken(
            String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }

    private String bytesToHex(
            byte[] bytes) {

        StringBuilder result =
                new StringBuilder();

        for (byte value : bytes) {

            result.append(
                    String.format(
                            "%02x",
                            value
                    )
            );
        }

        return result.toString();
    }
}