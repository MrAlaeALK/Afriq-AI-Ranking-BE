package com.pfa.pfaproject.service;

import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.model.PasswordResetToken;
import com.pfa.pfaproject.repository.PasswordResetTokenRepository;
import com.pfa.pfaproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Password Reset Service
 * ===========================================================
 * 
 * Service class for managing password reset functionality.
 * Handles token generation, validation, password updates, and cleanup.
 * 
 * Features:
 * - Secure token generation using cryptographically strong random numbers
 * - Rate limiting to prevent abuse
 * - Automatic cleanup of expired tokens
 * - Email integration for sending reset links
 * - Transaction management for data consistency
 * 
 * Security measures:
 * - Tokens expire after 1 hour
 * - Maximum 10 valid tokens per admin at any time
 * - Cryptographically secure token generation
 * - Single-use tokens
 * - New passwords must be different from current passwords
 * 
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_HOURS = 1;
    private static final int MAX_TOKENS_PER_ADMIN = 10;
    private static final int TOKEN_LENGTH = 32; // bytes, will be base64 encoded
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generates a password reset token for the given email.
     * 
     * @param email The email address of the admin
     * @return The generated token string
     * @throws CustomException if email not found or rate limit exceeded
     */
    public String generatePasswordResetToken(String email) {
        log.info("Generating password reset token for email: {}", email);
        
        // Find admin by email
        Admin admin = adminService.findByEmail(email);
        if (admin == null) {
            // For security, we don't reveal if the email exists or not
            // But we log it for admin purposes
            log.warn("Password reset requested for non-existent email: {}", email);
            throw new CustomException("If the email exists in our system, a reset link will be sent.", HttpStatus.OK);
        }
        
        // Check rate limiting
        long existingValidTokens = passwordResetTokenRepository
            .countValidTokensForAdmin(admin, LocalDateTime.now());
        
        if (existingValidTokens >= MAX_TOKENS_PER_ADMIN) {
            log.warn("Rate limit exceeded for admin: {} ({})", admin.getUsername(), email);
            throw new CustomException("Too many password reset requests. Please wait before requesting again.", HttpStatus.TOO_MANY_REQUESTS);
        }
        
        // Generate secure token
        String token = generateSecureToken();
        
        // Create and save token entity
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .admin(admin)
            .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
            .used(false)
            .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset token generated successfully for admin: {}", admin.getUsername());
        return token;
    }
    
    /**
     * Validates a password reset token.
     * 
     * @param token The token to validate
     * @return true if token is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        log.debug("Validating password reset token");
        
        Optional<PasswordResetToken> tokenEntity = passwordResetTokenRepository.findByToken(token);
        
        if (tokenEntity.isEmpty()) {
            log.warn("Invalid password reset token attempted");
            return false;
        }
        
        boolean isValid = tokenEntity.get().isValid();
        log.debug("Token validation result: {}", isValid);
        
        return isValid;
    }
    
    /**
     * Resets the password using a valid token.
     * 
     * @param token The reset token
     * @param newPassword The new password (will be encoded)
     * @return true if password was reset successfully
     * @throws CustomException if token is invalid or expired
     */
    public boolean resetPassword(String token, String newPassword) {
        log.info("Attempting password reset with token");
        
        Optional<PasswordResetToken> tokenEntity = passwordResetTokenRepository.findByToken(token);
        
        if (tokenEntity.isEmpty()) {
            log.warn("Password reset attempted with invalid token");
            throw new CustomException("Invalid or expired reset token.", HttpStatus.BAD_REQUEST);
        }
        
        PasswordResetToken resetToken = tokenEntity.get();
        
        if (!resetToken.isValid()) {
            log.warn("Password reset attempted with expired/used token for admin: {}", 
                resetToken.getAdmin().getUsername());
            throw new CustomException("Reset token has expired or has already been used.", HttpStatus.BAD_REQUEST);
        }
        
        Admin admin = resetToken.getAdmin();
        
        // Security check: Prevent setting the same password
        if (passwordEncoder.matches(newPassword, admin.getPassword())) {
            log.warn("Password reset attempted with same password for admin: {}", admin.getUsername());
            throw new CustomException("New password must be different from your current password.", HttpStatus.BAD_REQUEST);
        }
        
        // Update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(encodedPassword);
        adminService.save(admin);
        
        // Mark token as used
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        // Clean up all other tokens for this admin
        passwordResetTokenRepository.deleteByAdmin(admin);
        
        log.info("Password reset successfully for admin: {}", admin.getUsername());
        return true;
    }
    
    /**
     * Gets the admin associated with a valid token.
     * Used for displaying user information before password reset.
     * 
     * @param token The reset token
     * @return Admin if token is valid, null otherwise
     */
    @Transactional(readOnly = true)
    public Admin getAdminByToken(String token) {
        Optional<PasswordResetToken> tokenEntity = passwordResetTokenRepository.findByToken(token);
        
        if (tokenEntity.isPresent() && tokenEntity.get().isValid()) {
            return tokenEntity.get().getAdmin();
        }
        
        return null;
    }
    
    /**
     * Generates a cryptographically secure random token.
     * 
     * @return Base64-encoded secure token
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Scheduled task to clean up expired and used tokens.
     * Runs every hour to maintain database hygiene.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredTokens() {
        log.debug("Starting cleanup of expired password reset tokens");
        
        LocalDateTime now = LocalDateTime.now();
        int expiredTokens = passwordResetTokenRepository.deleteByExpiresAtBefore(now);
        int usedTokens = passwordResetTokenRepository.deleteByUsedTrue();
        
        if (expiredTokens > 0 || usedTokens > 0) {
            log.info("Cleaned up {} expired tokens and {} used tokens", expiredTokens, usedTokens);
        }
    }
} 