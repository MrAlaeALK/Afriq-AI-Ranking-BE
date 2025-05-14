package com.pfa.pfaproject.config.JWT;

import com.pfa.pfaproject.service.AdminService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * ===========================================================
 * 
 * This filter intercepts incoming requests to validate JWT tokens
 * in the Authorization header. For valid tokens, it sets up Spring Security
 * authentication context to authorize the request.
 * 
 * Key features:
 * - Extracts JWT token from Authorization header
 * - Validates token authenticity and expiration
 * - Sets up Spring Security context with user details and authorities
 * - Handles various token validation edge cases
 * 
 * @since 1.0
 * @version 1.1
 */
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminService adminService;
    private final String tokenPrefix;

    /**
     * Creates a new JWT filter with required dependencies.
     * 
     * @param jwtUtil JWT token utility for validation and parsing
     * @param adminService Service for loading user details
     */
    public JwtFilter(
            JwtUtil jwtUtil, 
            AdminService adminService,
            @Value("${jwt.token.prefix:Bearer }") String tokenPrefix) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.tokenPrefix = tokenPrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // Extract authorization header
            final String authHeader = request.getHeader("Authorization");
            String jwtToken = null;
            String username = null;

            // Check if header exists and has correct format
            if (authHeader != null && authHeader.startsWith(tokenPrefix)) {
                // Extract token (remove prefix)
                jwtToken = authHeader.substring(tokenPrefix.length());
                
                // Extract username from token
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                    log.debug("Extracted username from token: {}", username);
                } catch (Exception e) {
                    log.warn("Invalid JWT token: {}", e.getMessage());
                    // Don't set response status here, as it might be an endpoint that doesn't require authentication
                }
            }

            // Only proceed with authentication if username was extracted and no authentication exists yet
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    // Load user details
                    UserDetails userDetails = adminService.loadUserByUsername(username);
                    
                    // Validate token against user details
                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        // Create authentication token with authorities
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        // Add request details to authentication
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set the authentication in context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Authenticated user {}, setting security context", username);
                    } else {
                        log.warn("Token validation failed for user: {}", username);
                    }
                } catch (Exception e) {
                    log.error("Authentication error: {}", e.getMessage());
                    // Clear security context in case of error
                    SecurityContextHolder.clearContext();
                }
            }
            
            // Continue filter chain
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Filter error: {}", e.getMessage(), e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write("Authentication error occurred");
        }
    }
}

