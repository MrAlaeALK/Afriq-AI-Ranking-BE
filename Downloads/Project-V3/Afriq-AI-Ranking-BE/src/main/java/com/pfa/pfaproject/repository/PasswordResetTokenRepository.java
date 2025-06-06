package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.PasswordResetToken;
import com.pfa.pfaproject.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PasswordResetToken Repository
 * ===========================================================
 * 
 * Repository interface for managing password reset tokens.
 * Provides methods for token creation, validation, and cleanup.
 * 
 * @since 1.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Finds a password reset token by its token string.
     * 
     * @param token The token string to search for
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Finds all password reset tokens for a specific admin.
     * Useful for checking existing tokens before creating new ones.
     * 
     * @param admin The admin to search tokens for
     * @return List of tokens for the admin
     */
    Optional<PasswordResetToken> findByAdminAndUsedFalseAndExpiresAtAfter(
        Admin admin, 
        LocalDateTime currentTime
    );
    
    /**
     * Deletes all expired tokens.
     * Used for cleanup operations to maintain database hygiene.
     * 
     * @param currentTime Current time to compare against expiry
     * @return Number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :currentTime")
    int deleteByExpiresAtBefore(LocalDateTime currentTime);
    
    /**
     * Deletes all used tokens.
     * Used for cleanup operations to remove consumed tokens.
     * 
     * @return Number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.used = true")
    int deleteByUsedTrue();
    
    /**
     * Deletes all tokens for a specific admin.
     * Useful when an admin's password is successfully reset.
     * 
     * @param admin The admin whose tokens should be deleted
     * @return Number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.admin = :admin")
    int deleteByAdmin(Admin admin);
    
    /**
     * Counts unexpired, unused tokens for an admin.
     * Used for rate limiting password reset requests.
     * 
     * @param admin The admin to count tokens for
     * @param currentTime Current time to compare against expiry
     * @return Number of valid tokens
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.admin = :admin AND p.used = false AND p.expiresAt > :currentTime")
    long countValidTokensForAdmin(Admin admin, LocalDateTime currentTime);
} 