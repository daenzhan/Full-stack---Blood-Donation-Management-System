package org.example.bloodrequestservice;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;


    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error creating signing key", e);
            throw new RuntimeException("Failed to create JWT signing key", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("Token is null or empty");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token validation successful");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        try {
            String email = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.debug("Extracted email from token: {}", email);
            return email;
        } catch (Exception e) {
            log.error("Failed to extract email from token", e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public String extractRole(String token) {
        try {
            String role = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
            log.debug("Extracted role from token: {}", role);
            return role;
        } catch (Exception e) {
            log.error("Failed to extract role from token", e);
            return "UNKNOWN";
        }
    }

    public Long extractUserId(String token) {
        try {
            Long userId = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class);
            log.debug("Extracted userId from token: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract userId from token", e);
            return null;
        }
    }
}