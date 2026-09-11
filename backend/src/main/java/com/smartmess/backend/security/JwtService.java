package com.smartmess.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.smartmess.backend.enums.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtService(
            JwtProperties jwtProperties,
            Clock clock) {

        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(
            String email,
            UserRole role) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "role",
                role.name()
        );

        Instant nowInstant =
                clock.instant();

        Date now =
                Date.from(nowInstant);

        Date expiration =
                Date.from(
                        nowInstant.plusMillis(
                                jwtProperties.getExpiration()
                        )
                );

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        return claimsResolver.apply(
                claims
        );
    }

    public String extractEmail(
            String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public UserRole extractRole(
            String token) {

        String role =
                extractClaim(
                        token,
                        claims ->
                                claims.get(
                                        "role",
                                        String.class
                                )
                );

        return UserRole.valueOf(
                role
        );
    }

    public Date extractExpiration(
            String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    public boolean isTokenExpired(
            String token) {

        return extractExpiration(
                token
        ).before(
                Date.from(
                        clock.instant()
                )
        );
    }

    public boolean isTokenValid(
            String token) {

        return !isTokenExpired(
                token
        );
    }

    public boolean isTokenValid(
            String token,
            String email) {

        return extractEmail(
                token
        ).equals(
                email
        )
                && !isTokenExpired(
                        token
                );
    }
}