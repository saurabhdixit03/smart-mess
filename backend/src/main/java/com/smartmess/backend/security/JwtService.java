package com.smartmess.backend.security;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.smartmess.backend.enums.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.function.Function;

import io.jsonwebtoken.Claims;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
    
    public String generateToken(
            String mobileNumber,
            UserRole role) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", role.name());

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + jwtProperties.getExpiration()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(mobileNumber)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }
    
    public String extractMobileNumber(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }
    
    public UserRole extractRole(String token) {

        String role = extractClaim(
                token,
                claims -> claims.get("role", String.class)
        );

        return UserRole.valueOf(role);
    }
    
    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }
    
    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }
    
    public boolean isTokenValid(String token) {

        return !isTokenExpired(token);
    }
}