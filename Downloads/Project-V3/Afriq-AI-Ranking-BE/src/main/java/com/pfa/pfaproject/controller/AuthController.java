package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Admin.ForgotPasswordDTO;
import com.pfa.pfaproject.dto.Admin.LoginDTO;
import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.dto.Admin.ResetPasswordDTO;
import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.service.AdminBusinessService;
import com.pfa.pfaproject.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Controller handling authentication operations for the Afriq-AI Ranking system.
 * 
 * Provides endpoints for administrator registration, login, and password reset.
 * These endpoints are accessible without authentication, allowing new users to 
 * register and existing users to log in or reset their passwords.
 * 
 * @since 1.0
 * @version 1.2
 */
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AdminBusinessService adminBusinessService;
    private final PasswordResetService passwordResetService;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Registers a new administrator user.
     * 
     * @param adminToRegister DTO containing registration details with password confirmation
     * @return JWT token for the newly registered admin
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO adminToRegister) {
        String jwt = adminBusinessService.register(adminToRegister);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(jwt));
    }

    /**
     * Authenticates an existing administrator user.
     * 
     * @param adminToLogin DTO containing login credentials
     * @return JWT token for the authenticated admin
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO adminToLogin) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.login(adminToLogin)));
    }

    /**
     * Initiates password reset process by sending reset email.
     * 
     * @param forgotPasswordRequest DTO containing the email address
     * @return Success message (same response regardless of email existence for security)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordRequest) {
        try {
            // Generate password reset token
            String resetToken = passwordResetService.generatePasswordResetToken(forgotPasswordRequest.email());
            
            // Get admin details for email
            Admin admin = passwordResetService.getAdminByToken(resetToken);
            if (admin != null) {
                // Send password reset email via EmailController
                String emailServiceUrl = "http://localhost:8080/api/email/send-password-reset";
                String requestUrl = String.format("%s?adminEmail=%s&adminName=%s&resetToken=%s&frontendBaseUrl=%s",
                    emailServiceUrl,
                    admin.getEmail(),
                    admin.getFirstName() + " " + admin.getLastName(),
                    resetToken,
                    "http://localhost:5173"
                );
                
                ResponseEntity<String> emailResponse = restTemplate.postForEntity(requestUrl, null, String.class);
                
                if (emailResponse.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.ok(ResponseWrapper.success("If the email exists in our system, a reset link will be sent."));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResponseWrapper.error("Failed to send reset email."));
                }
            }
            
            // Always return success message for security (don't reveal if email exists)
            return ResponseEntity.ok(ResponseWrapper.success("If the email exists in our system, a reset link will be sent."));
            
        } catch (Exception e) {
            // Always return success message for security
            return ResponseEntity.ok(ResponseWrapper.success("If the email exists in our system, a reset link will be sent."));
        }
    }

    /**
     * Validates a password reset token.
     * 
     * @param token The reset token to validate
     * @return Validation result and admin info if valid
     */
    @GetMapping("/verify-reset-token/{token}")
    public ResponseEntity<?> verifyResetToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        
        if (isValid) {
            Admin admin = passwordResetService.getAdminByToken(token);
            return ResponseEntity.ok(ResponseWrapper.success(
                "Token is valid for user: " + admin.getFirstName() + " " + admin.getLastName()
            ));
        } else {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Invalid or expired reset token."));
        }
    }

    /**
     * Resets password using a valid reset token.
     * 
     * @param resetPasswordRequest DTO containing token and new password
     * @return Success message if password was reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordRequest) {
        // Validate that passwords match
        if (!resetPasswordRequest.passwordsMatch()) {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error("Passwords do not match."));
        }
        
        try {
            boolean success = passwordResetService.resetPassword(
                resetPasswordRequest.token(), 
                resetPasswordRequest.newPassword()
            );
            
            if (success) {
                return ResponseEntity.ok(ResponseWrapper.success("Password has been reset successfully."));
            } else {
                return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Failed to reset password."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ResponseWrapper.error(e.getMessage()));
        }
    }
}
