package com.cropdeal.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Responsible for two things only: generating JWTs and validating them.
 * Nothing else. This class has no idea about HTTP requests, databases,
 * or Spring Security filter chains -- it's a pure utility that takes
 * inputs and returns outputs.
 *
 * How a JWT works (simplified):
 *   header.payload.signature
 *   - header: says "this is a JWT signed with HMAC-SHA256"
 *   - payload: carries claims (who is this user, what role, when does
 *     this expire) -- readable by anyone, NOT encrypted
 *   - signature: proves the payload wasn't tampered with after we issued
 *     it -- only someone with our secret key can produce or verify this
 */
@Component
public class JwtUtil {

    // Value injected from application.yml's jwt.secret property.
    // The @Value annotation pulls it in at startup -- if the property
    // is missing, startup fails immediately with a clear error rather
    // than failing silently at runtime when the first token is issued.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        // Derives a cryptographic key from the secret string.
        // Keys.hmacShaKeyFor requires at least 256 bits (32 bytes) --
        // if your jwt.secret in application.yml is shorter than 32
        // characters, this will throw an error at startup.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT for the given user.
     * The token carries: email (subject), role (custom claim),
     * issued-at time, and expiry time.
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Pulls the email (subject) out of a token.
     * Throws an exception automatically if the token is expired
     * or the signature doesn't match -- we don't need to check
     * those conditions manually.
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            // Any exception (expired, bad signature, malformed) means
            // the token is not valid -- return false rather than
            // propagating the exception to callers.
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}