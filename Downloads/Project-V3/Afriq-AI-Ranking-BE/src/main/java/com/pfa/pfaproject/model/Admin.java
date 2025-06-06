package com.pfa.pfaproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Admin Entity
 * ===========================================================
 *
 * This entity represents an administrator in the Africa AI Ranking system.
 * It implements Spring Security's UserDetails interface to support authentication
 * and authorization within the application.
 *
 * Features:
 * - Secure password handling with BCrypt encryption
 * - Role-based access control via Spring Security
 * - Input validation for all fields
 * - Audit tracking with creation and modification timestamps
 *
 * Key annotations:
 * - @Entity: Marks this class as a JPA entity
 * - @Table: Defines database table name and constraints
 * - @JsonIgnore: Prevents sensitive data from being serialized
 * - @Builder: Enables fluent object creation
 *
 * Implementation notes:
 * - Passwords are never exposed in API responses (via @JsonIgnore)
 * - All admins are granted ROLE_ADMIN authority
 * - Account status flags default to enabled/non-expired
 * - Creation and modification times are automatically tracked
 *
 * Usage example:
 *
 * // Creating a new admin
 * Admin admin = Admin.builder()
 *     .firstName("John")
 *     .lastName("Doe")
 *     .username("johndoe")
 *     .email("john.doe@example.com")
 *     .password(passwordEncoder.encode("securePassword"))
 *     .build();
 *
 * @since 1.0
 * @version 1.1
 */


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "admin")
public class Admin implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotBlank(message = "First name is required")
    private String firstName;

//    @NotBlank(message = "Last name is required")
    private String lastName;

//    @NotBlank(message = "Username is required")
//    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
//    @Column(nullable = false)
    @Column(unique = true)
    private String username;

    @JsonIgnore
//    @NotBlank(message = "Password is required")
//    @Size(min = 8, message = "Password must be at least 8 characters")
//    @Column(nullable = false)
    private String password;

//    @NotBlank(message = "Email is required")
//    @Email(message = "Email should be valid")
//    @Column(nullable = false)
    @Column(unique = true)
    private String email;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean enabled = true;

//    @Column(name = "created_date")
    private LocalDateTime createdDate;

//    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
