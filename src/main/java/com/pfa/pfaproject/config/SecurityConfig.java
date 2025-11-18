package com.pfa.pfaproject.config;

import com.pfa.pfaproject.config.JWT.JwtFilter;
import com.pfa.pfaproject.service.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final AdminService adminService;
    private final JwtFilter jwtFilter;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    public SecurityConfig(AdminService adminService, JwtFilter jwtFilter) {
        this.adminService = adminService;
        this.jwtFilter = jwtFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF protection since we're using stateless JWT tokens
                // and not relying on session cookies which CSRF typically protects
                .csrf(AbstractHttpConfigurer::disable)
                
                // Enable CORS with the corsConfigurationSource bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configure security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(frame -> frame.sameOrigin())
                        .cacheControl(cache -> cache.disable()))
                
                // Configure authorization rules
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN") // return it to hasRole later
                        .anyRequest().permitAll())
                
                // Set authentication provider
                .authenticationProvider(authenticationProvider())
                
                // Use stateless session management (no HTTP session for authentication)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Add JWT filter before the standard authentication filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }

    // Password encoder bean for securely hashing passwords
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(adminService);
        return authProvider;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authenticationProvider());
        return auth.build();
    }
    
    /**
     * Configures CORS settings for the application.
     * Uses environment variable FRONTEND_URL to allow specific origins.
     * In development: http://localhost:5173
     * In production: https://your-frontend-app.netlify.app (For example)
     * 
     * @return The CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific frontend URL from environment variable
        List<String> allowedOrigins = Arrays.asList(frontendUrl.split(","));
        configuration.setAllowedOrigins(allowedOrigins);
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Don't allow credentials since we're using JWT in Authorization header
        configuration.setAllowCredentials(false);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}