package com.pfa.pfaproject.config.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

/**
 * Utility class for JWT token generation, validation, and parsing.
 * Provides methods to create access tokens, refresh tokens, and validate tokens for authentication.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${jwt.expirationtime}")
    private Long accessTokenExpirationTime;

    
    /**
     * Gets the secret key for JWT signing from the base64-encoded configuration property.
     * @return SecretKey for JWT operations
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates an access token for a user.
     * @param subject The username or subject identifier
     * @param authorities The user's granted authorities/roles
     * @return JWT access token string
     */
    public String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        log.debug("Generating access token for user: {}", subject);
        String token = createToken(subject, authorities, accessTokenExpirationTime, "ACCESS");
        log.info("Access token generated successfully for user: {}", subject);
        return token;
    }
    
    /**
     * Creates a JWT token with specified parameters.
     * @param subject The username or subject identifier
     * @param authorities The user's authorities (null for refresh tokens)
     * @param expirationTime The token expiration time in milliseconds
     * @param tokenType The type of token (ACCESS or REFRESH)
     * @return JWT token string
     */
    private String createToken(String subject, Collection<?> authorities, Long expirationTime, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        Map<String, Object> claims = new HashMap<>();
        if (authorities != null) {
            claims.put("roles", authorities);
        }
        claims.put("type", tokenType);
        
        try {
            return Jwts.builder()
                    .claims()
                    .add(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .id(UUID.randomUUID().toString()) // Add JWT ID for uniqueness
                    .and()
                    .signWith(getSecretKey())
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token for user: {}", subject, e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    /**
     * Extracts the username from a JWT token.
     * @param token The JWT token
     * @return The subject (username) from the token
     * @throws JwtException if token is invalid
     */
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Attempt to extract username from expired token");
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            throw e;
        }
    }

    /**
     * Extracts all claims from a JWT token.
     * @param token The JWT token
     * @return All claims from the token
     * @throws JwtException if token is invalid
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse token", e);
            throw new JwtException("Failed to parse token", e);
        }
    }

    /**
     * Validates a token for a specific user.
     * @param token The JWT token
     * @param userDetails The user details to validate against
     * @return true if token is valid for the user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            
            return username.equals(userDetails.getUsername()) 
                   && !isTokenExpired(token) 
                   && "ACCESS".equals(tokenType);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for user: {}", userDetails.getUsername());
            return false;
        } catch (Exception e) {
            log.error("JWT token validation error for user: {}", userDetails.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Extracts the token type from a JWT token.
     * @param token The JWT token
     * @return The token type (ACCESS or REFRESH)
     */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * Checks if a token has expired.
     * @param token The JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }
}

