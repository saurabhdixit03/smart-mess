package com.smartmess.backend.config;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.security.CustomUserDetails;
import com.smartmess.backend.security.CustomUserDetailsService;
import com.smartmess.backend.security.JwtService;

@Component
public class WebSocketAuthInterceptor
        implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService) {

        this.jwtService = jwtService;
        this.customUserDetailsService =
                customUserDetailsService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        StompCommand command =
                accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {

            authenticateConnection(accessor);

        } else if (StompCommand.SUBSCRIBE.equals(command)) {

            authorizeSubscription(accessor);
        }

        return message;
    }

    /*
     * Authenticates the STOMP connection using
     * the JWT supplied in the Authorization header.
     */
    private void authenticateConnection(
            StompHeaderAccessor accessor) {

        String authorizationHeader =
                accessor.getFirstNativeHeader(
                        "Authorization"
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new IllegalArgumentException(
                    "WebSocket authentication token is required."
            );
        }

        String token =
                authorizationHeader.substring(7);

        String email =
                jwtService.extractEmail(token);

        UserRole role =
                jwtService.extractRole(token);

        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByEmail(
                                email,
                                role
                        );

        if (!jwtService.isTokenValid(
                token,
                userDetails.getUsername())) {

            throw new IllegalArgumentException(
                    "Invalid or expired WebSocket authentication token."
            );
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        accessor.setUser(authentication);
    }

    /*
     * Authorizes subscriptions according to
     * the authenticated user's application role.
     */
    private void authorizeSubscription(
            StompHeaderAccessor accessor) {

        Principal principal =
                accessor.getUser();

        if (principal == null) {

            throw new IllegalArgumentException(
                    "Authenticated WebSocket user is required."
            );
        }

        if (!(principal
                instanceof UsernamePasswordAuthenticationToken authentication)) {

            throw new IllegalArgumentException(
                    "Invalid WebSocket authentication."
            );
        }

        if (!(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)) {

            throw new IllegalArgumentException(
                    "Invalid WebSocket user details."
            );
        }

        String destination =
                accessor.getDestination();

        if (destination == null) {

            throw new IllegalArgumentException(
                    "WebSocket subscription destination is required."
            );
        }

        UserRole role =
                userDetails.getRole();

        if (destination.startsWith(
                "/topic/dashboard/")) {

            if (role != UserRole.OWNER) {

                throw new IllegalArgumentException(
                        "Only owners can subscribe to dashboard updates."
                );
            }

            return;
        }

        if (destination.equals(
                "/user/queue/notifications")) {

            if (role != UserRole.CUSTOMER) {

                throw new IllegalArgumentException(
                    "Only customers can subscribe to notifications."
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "WebSocket subscription destination is not allowed: "
                        + destination
        );
    }
}