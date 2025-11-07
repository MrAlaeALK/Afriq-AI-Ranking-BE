package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Admin.*;
import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.service.AdminBusinessService;
import com.pfa.pfaproject.service.PasswordResetService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Controller handling authentication operations for the Afriq-AI Ranking system.
 * 
 * Provides endpoints for administrator registration and login. These endpoints
 * are accessible without authentication, allowing new users to register and
 * existing users to log in to the system.
 * 
 * @since 1.0
 * @version 1.1
 */
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AdminBusinessService adminBusinessService;
    private final PasswordResetService passwordResetService;
    private final RestTemplate restTemplate;

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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequestDTO refreshRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.refreshAccessToken(refreshRequestDTO)));
    }
    //for future use if we managed to make httpOnly cookie
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO adminToLogin) {
//        LoginResponseDTO loginResponseDTO = adminBusinessService.login(adminToLogin);
//
//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponseDTO.refreshToken())
//                .httpOnly(true)
////                .secure(false) // set to false for local testing (no HTTPS)
//                .secure(true)
//                .path("/")    // make sure it's available where you need it
//                .maxAge(Duration.ofDays(1)) // 7-day expiry
////                .sameSite("Strict") // or "Lax" if you want it sent with some cross-origin requests
////                .maxAge(80)
//                .sameSite("None")
//                .build();
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//                .body(ResponseWrapper.success(new LoginResponseDTO(loginResponseDTO.accessToken(), null))); // don't expose refresh token in body
////        return ResponseEntity.status(HttpStatus.OK)
////                .body(ResponseWrapper.success(adminBusinessService.login(adminToLogin)));
//    }
//
//    @PostMapping("/refresh-token")
//    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
//        if (refreshToken == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body("Missing refresh token");
//        }
//
//        String newAccessToken = adminBusinessService.refreshAccessToken(new RefreshRequestDTO(refreshToken));
//
//        return ResponseEntity.ok(ResponseWrapper.success(newAccessToken));
//
////        try {
////            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
////            return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, null)); // no new refreshToken here
////        } catch (JwtException ex) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
////                    .body("Invalid or expired refresh token");
////        }
////        return ResponseEntity.status(HttpStatus.OK)
////                .body(ResponseWrapper.success(adminBusinessService.refreshAccessToken(refreshRequestDTO)));
//    }

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
