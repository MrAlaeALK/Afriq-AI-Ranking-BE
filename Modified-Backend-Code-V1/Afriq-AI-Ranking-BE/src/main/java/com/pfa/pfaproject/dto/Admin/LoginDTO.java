package com.pfa.pfaproject.dto.Admin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for admin login operations.
 * Contains the credentials needed for authentication.
 */
public record LoginDTO(
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,
        
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
