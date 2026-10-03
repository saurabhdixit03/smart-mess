package com.smartmess.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.smartmess.backend.security.JwtAuthenticationFilter;
import com.smartmess.backend.security.SecurityExceptionHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider,
            SecurityExceptionHandler securityExceptionHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(
                                securityExceptionHandler
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Authentication
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Public mess information for customer registration
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/public/messes/registration/*"
                        )
                        .permitAll()

                        // Owner's registration link for sharing and QR rendering
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/mess/registration-link"
                        )
                        .hasRole("OWNER")

                        // SockJS / WebSocket handshake
                        .requestMatchers("/ws-dashboard/**")
                        .permitAll()

                        // Everything else requires JWT
                        .anyRequest()
                        .authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}