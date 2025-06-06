package com.pfa.pfaproject.dto.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Reset Password DTO
 * ===========================================================
 * 
 * Data Transfer Object for password reset requests.
 * Contains the reset token and new password information.
 *
 */
public record ResetPasswordDTO(
    @NotBlank(message = "Reset token is required")
    String token,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).*$",
        message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character (@#$%^&+=!*)"
    )
    String newPassword,
    
    @NotBlank(message = "Password confirmation is required")
    String confirmPassword
) {
    /**
     * Validates that password and confirmation match.
     * This method should be called in the service layer for validation.
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
} 