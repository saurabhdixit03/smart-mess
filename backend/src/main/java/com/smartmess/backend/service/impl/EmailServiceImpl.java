package com.smartmess.backend.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final RestClient brevoClient;
    private final String mailFrom;
    private final String frontendUrl;
    private final int resetExpirationMinutes;

    public EmailServiceImpl(
            @Value("${app.mail.brevo.api-key}") String apiKey,
            @Value("${app.mail.from}") String mailFrom,
            @Value("${app.frontend.url}") String frontendUrl,
            @Value("${app.password-reset.expiration-minutes:30}")
            int resetExpirationMinutes) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Brevo API key is required."
            );
        }

        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.resetExpirationMinutes = resetExpirationMinutes;

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);

        this.brevoClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .requestFactory(requestFactory)
                .defaultHeader("api-key", apiKey)
                .defaultHeader(
                        "Accept",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetToken,
            UserRole userRole) {

        String resetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/reset-password")
                .queryParam("token", resetToken)
                .queryParam("role", userRole.name())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        String textContent = """
                Reset your Smart Mess password

                We received a request to reset your password.

                Set a new password using this link:
                %s

                This link expires in %d minutes and can only be used once.

                If you did not request this, you can ignore this email.
                Your password will remain unchanged.

                Smart Mess
                """.formatted(resetUrl, resetExpirationMinutes);

        String bodyContent = """
                <p style="margin:0 0 16px;">
                  We received a request to reset your Smart Mess password.
                </p>
                <p style="margin:0 0 24px;">
                  Use the button below to choose a new password.
                </p>
                """;

        String htmlContent = buildEmail(
                "Password reset",
                "Set a new password",
                bodyContent,
                "Reset Password",
                resetUrl,
                "This link expires in "
                        + resetExpirationMinutes
                        + " minutes and can only be used once. "
                        + "If you did not request this, ignore this email. "
                        + "Your password will remain unchanged."
        );

        sendEmail(
                recipientEmail,
                "Reset your Smart Mess password",
                textContent,
                htmlContent
        );
    }

    @Override
    public void sendCustomerApprovalEmail(
            String recipientEmail,
            String customerName,
            String messName) {

        String loginUrl = frontendUrl + "/customer/login";

        String textContent = """
                Hi %s,

                Your registration with %s has been approved.

                You can now view menus, respond to meals,
                and access your meal history and bills.

                Sign in here:
                %s

                Use the email address and password you chose during registration.

                Welcome aboard!
                Smart Mess
                """.formatted(customerName, messName, loginUrl);

        String bodyContent = """
                <p style="margin:0 0 16px;">Hi %s,</p>
                <p style="margin:0 0 16px;">
                  Your registration with <strong>%s</strong>
                  has been approved.
                </p>
                <p style="margin:0 0 24px;">
                  You can now view menus, respond to meals,
                  and access your meal history and bills.
                </p>
                """.formatted(
                        escapeHtml(customerName),
                        escapeHtml(messName)
                );

        String htmlContent = buildEmail(
                "Registration approved",
                "You're ready to join your mess",
                bodyContent,
                "Sign In",
                loginUrl,
                "Use the email address and password "
                        + "you chose during registration."
        );

        sendEmail(
                recipientEmail,
                "Your Smart Mess registration is approved",
                textContent,
                htmlContent
        );
    }

    @Override
    public void sendCustomerNotificationEmail(
            String recipientEmail,
            String customerName,
            String messName,
            NotificationType notificationType,
            String title,
            String message) {

        if (notificationType != NotificationType.BILL_GENERATED
                && notificationType != NotificationType.PAYMENT_RECEIVED
                && notificationType != NotificationType.MEAL_PRICING) {

            throw new IllegalArgumentException(
                    "This notification type does not support email delivery."
            );
        }

        if (recipientEmail == null || recipientEmail.isBlank()
                || title == null || title.isBlank()
                || message == null || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Email recipient, title and message are required."
            );
        }

        boolean pricing =
                notificationType == NotificationType.MEAL_PRICING;

        String actionUrl = frontendUrl
                + (pricing
                        ? "/customer/mess-details"
                        : "/customer/my-bills");

        String buttonLabel =
                pricing ? "View Mess Details" : "View My Bills";

        String textContent = """
                Hi %s,

                %s
                %s

                %s

                Open your customer portal:
                %s

                Sign in to view your account details.

                Smart Mess
                """.formatted(
                        customerName == null ? "" : customerName,
                        messName == null ? "" : messName,
                        title,
                        message,
                        actionUrl
                );

        String bodyContent = """
                <p style="margin:0 0 16px;">Hi %s,</p>
                <p style="margin:0 0 16px;">
                  An update from <strong>%s</strong>.
                </p>
                <p style="margin:0 0 24px;white-space:pre-line;">%s</p>
                """.formatted(
                        escapeHtml(customerName),
                        escapeHtml(messName),
                        escapeHtml(message)
                );

        String htmlContent = buildEmail(
                messName == null ? "Customer update" : messName,
                title,
                bodyContent,
                buttonLabel,
                actionUrl,
                "Sign in to view your account details."
        );

        String subject = ("Smart Mess — " + title)
                .replace('\r', ' ')
                .replace('\n', ' ');

        sendEmail(
                recipientEmail,
                subject,
                textContent,
                htmlContent
        );
    }

    private void sendEmail(
            String recipientEmail,
            String subject,
            String textContent,
            String htmlContent) {

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", "Smart Mess",
                        "email", mailFrom
                ),
                "to", List.of(
                        Map.of("email", recipientEmail)
                ),
                "subject", subject,
                "textContent", textContent,
                "htmlContent", htmlContent
        );

        brevoClient.post()
                .uri("/smtp/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildEmail(
            String eyebrow,
            String heading,
            String bodyContent,
            String buttonLabel,
            String actionUrl,
            String note) {

        String safeUrl = escapeHtml(actionUrl);

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport"
                        content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#f5f6f8;
                             font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" width="100%%"
                         cellpadding="0" cellspacing="0">
                    <tr>
                      <td align="center" style="padding:32px 16px;">
                        <table role="presentation" width="100%%"
                               cellpadding="0" cellspacing="0"
                               style="max-width:560px;background:#ffffff;
                                      border:1px solid #e5e7eb;
                                      border-radius:16px;">
                          <tr>
                            <td style="padding:28px 28px 20px;
                                       border-bottom:1px solid #e5e7eb;">
                              <span style="font-size:20px;font-weight:700;
                                           color:#166534;">
                                Smart Mess
                              </span>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px;color:#374151;
                                       font-size:15px;line-height:1.7;">
                              <p style="margin:0 0 8px;color:#166534;
                                        font-size:12px;font-weight:700;
                                        letter-spacing:1px;
                                        text-transform:uppercase;">
                                %s
                              </p>
                              <h1 style="margin:0 0 24px;color:#111827;
                                         font-size:25px;line-height:1.3;">
                                %s
                              </h1>

                              %s

                              <table role="presentation"
                                     cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="background:#166534;
                                             border-radius:8px;">
                                    <a href="%s"
                                       style="display:inline-block;
                                              padding:13px 24px;
                                              color:#ffffff;
                                              font-weight:700;
                                              text-decoration:none;">
                                      %s
                                    </a>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:24px 0 0;font-size:13px;
                                        color:#6b7280;">
                                %s
                              </p>
                              <p style="margin:20px 0 4px;font-size:12px;
                                        color:#6b7280;">
                                If the button doesn't work, open this link:
                              </p>
                              <a href="%s"
                                 style="font-size:12px;color:#166534;
                                        word-break:break-all;">
                                %s
                              </a>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:20px 28px;
                                       border-top:1px solid #e5e7eb;
                                       color:#6b7280;font-size:12px;">
                              Smart Mess · Meal planning made simpler
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                        escapeHtml(eyebrow),
                        escapeHtml(heading),
                        bodyContent,
                        safeUrl,
                        escapeHtml(buttonLabel),
                        escapeHtml(note),
                        safeUrl,
                        safeUrl
                );
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}