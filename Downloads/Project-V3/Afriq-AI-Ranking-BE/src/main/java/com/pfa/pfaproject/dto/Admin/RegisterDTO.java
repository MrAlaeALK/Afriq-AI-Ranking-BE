package com.pfa.pfaproject.dto.Admin;

import com.pfa.pfaproject.validation.PasswordsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * Data Transfer Object for admin registration.
 * Contains validated user details required for creating a new admin account.
 * Enforces validation rules for each field to ensure data integrity.
 */
@PasswordsMatch(message = "Password and confirmation must match")
public record RegisterDTO(
        @NotBlank(message = "First name is required")
        String firstName,
        
        @NotBlank(message = "Last name is required")
        String lastName,
        
        @NotBlank(message = "Username is required")
        @Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", 
                message = "Username must contain only letters, numbers, and underscores")
        String username,
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        
        @NotBlank(message = "Password is required")
        @Length(min = 8, max = 100, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).*$", 
                message = "Password must contain at least one digit, lowercase letter, uppercase letter, and special character")
        String password,
        
        @NotBlank(message = "Password confirmation is required")
        String passwordConfirmation
) {
}
