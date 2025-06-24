package com.pfa.pfaproject.config;

import com.pfa.pfaproject.config.JWT.JwtFilter;
import com.pfa.pfaproject.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

/**
 * Security Configuration for the Afriq-AI Ranking system.
 * ===========================================================
 * 
 * This class configures Spring Security for the application, including:
 * - JWT-based stateless authentication
 * - URL-based authorization rules
 * - CORS and CSRF settings
 * - Security headers
 * - Password encoding
 * 
 * The system uses a stateless authentication approach with JWT tokens
 * rather than session-based authentication, which is more suitable for
 * REST APIs and allows for better scalability.
 * 
 * @since 1.0
 * @version 1.1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final AdminService adminService;
    private final JwtFilter jwtFilter;
    
    /**
     * Configures the security filter chain with authorization rules,
     * authentication providers, and security features.
     * 
     * @param http The HttpSecurity to configure
     * @return The configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
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
                        // Public endpoints
//                        .requestMatchers("/api/v1/auth/**").permitAll()
//                        .requestMatchers("/actuator/health").permitAll()
//                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        // Protected endpoints
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/v1/countries/**", "/api/v1/indicators/**").authenticated()
//                        // All other requests need authentication
//                        .anyRequest().authenticated())
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // return it to hasRole later
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

    /**
     * Creates a password encoder bean for securely hashing passwords.
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures and returns the authentication provider.
     * @return The configured authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(adminService);
        return authProvider;
    }

    /**
     * Creates a RestTemplate bean for making HTTP requests.
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates the authentication manager.
     * @param http The HttpSecurity to get the shared objects from
     * @return The configured authentication manager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authenticationProvider());
        return auth.build();
    }
    
    /**
     * Configures CORS settings for the application.
     * @return The CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // In production, restrict to specific origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false); // Since we're using JWT, not session cookies
        configuration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
    //for future use (httpOnly cookie for refresh token)
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // In production, restrict to specific origins
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Cookie"));
//        configuration.setExposedHeaders(List.of("Authorization"));
//        configuration.setAllowCredentials(true); // Since we're using JWT, not session cookies
//        configuration.setMaxAge(3600L); // 1 hour
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

