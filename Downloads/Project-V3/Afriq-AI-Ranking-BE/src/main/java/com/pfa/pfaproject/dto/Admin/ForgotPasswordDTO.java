package com.pfa.pfaproject.dto.Admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Forgot Password DTO
 * ===========================================================
 * 
 * Data Transfer Object for forgot password requests.
 * Contains the email address of the admin requesting a password reset.
 * 
 * Validation rules:
 * - Email must not be blank
 * - Email must be in valid format
 * 
 * @since 1.0
 */
public record ForgotPasswordDTO(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email
) {} 