package com.pfa.pfaproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PasswordResetToken Entity
 * ===========================================================
 * 
 * This entity represents a password reset token for admin users.
 * Tokens are generated when admins request password resets and expire
 * after a configurable time period for security.
 *
 * @since 1.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "password_reset_token")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The unique token string sent to the user's email.
     * This should be cryptographically secure and unpredictable.
     */
    @NotBlank(message = "Token is required")
    @Column(unique = true, nullable = false, length = 255)
    private String token;
    
    /**
     * The admin user this token is for.
     * Each token is tied to a specific admin account.
     */
    @NotNull(message = "Admin is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;
    
    /**
     * When this token expires.
     * After this time, the token cannot be used.
     */
    @NotNull(message = "Expiry time is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Whether this token has been used.
     * Once used, tokens cannot be reused for security.
     */
    @Builder.Default
    @Column(name = "used", nullable = false)
    private Boolean used = false;
    
    /**
     * When this token was created.
     * Used for audit trails and cleanup operations.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default to 1 hour expiration
            expiresAt = createdAt.plusHours(1);
        }
    }
    
    /**
     * Checks if this token is still valid.
     * A token is valid if it hasn't been used and hasn't expired.
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Marks this token as used.
     * This should be called when the token is successfully consumed.
     */
    public void markAsUsed() {
        this.used = true;
    }
} 