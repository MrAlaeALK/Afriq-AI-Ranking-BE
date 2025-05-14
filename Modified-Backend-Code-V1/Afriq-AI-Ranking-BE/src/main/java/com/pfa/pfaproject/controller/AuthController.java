package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Admin.LoginDTO;
import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.service.AdminBusinessService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
