package com.smartmess.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetToken,
            UserRole userRole) {

        String resetUrl =
                frontendUrl
                        + "/reset-password?token="
                        + resetToken
                        + "&role="
                        + userRole.name();

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(recipientEmail);

        message.setSubject(
                "Reset your Smart Mess password"
        );

        message.setText(
                """
                We received a request to reset your Smart Mess password.

                Use the link below to set a new password:

                %s

                This link will expire shortly and can only be used once.

                If you did not request a password reset, you can ignore this email.

                Smart Mess
                """.formatted(resetUrl)
        );

        mailSender.send(message);
    }

    @Override
    public void sendCustomerApprovalEmail(
            String recipientEmail,
            String customerName,
            String messName) {

        String loginUrl =
                frontendUrl.replaceAll("/+$", "")
                        + "/customer/login";

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(recipientEmail);

        message.setSubject(
                "Your Smart Mess registration is approved"
        );

        message.setText(
                """
                Hi %s,

                Your registration with %s has been approved.

                You can now sign in to view menus, respond to meals,
                and access your meal history and bills.

                Sign in here:
                %s

                Use the email address and password you chose during registration.

                Welcome aboard!

                Smart Mess
                """.formatted(
                        customerName,
                        messName,
                        loginUrl
                )
        );

        mailSender.send(message);
    }
}