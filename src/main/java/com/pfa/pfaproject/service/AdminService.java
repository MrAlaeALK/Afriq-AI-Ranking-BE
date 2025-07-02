package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.repository.AdminRepository;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Admin entities and providing UserDetails for Spring Security.
 * ===========================================================
 * 
 * This service handles all operations related to Admin entities including:
 * - CRUD operations for administrator management
 * - Authentication and user details for Spring Security
 * - Username and email validation
 * 
 * As this service implements UserDetailsService, it serves as the bridge
 * between the application's admin model and Spring Security authentication.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;

    // ========== QUERY METHODS ==========

    /**
     * Returns all admin users in the system.
     * @return List of all admins
     */
    public List<Admin> findAll() {
        log.info("Retrieving all admins");
        return adminRepository.findAll();
    }

    /**
     * Finds an admin by their ID.
     * @param id The admin ID
     * @return The found admin
     * @throws CustomException if admin is not found
     */
    public Admin findById(Long id) {
        log.info("Finding admin with ID: {}", id);
        return adminRepository.findById(id)
                .orElseThrow(() -> new CustomException("Admin not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds admin by username.
     * @param username The username to search
     * @return Admin if found, null otherwise
     */
    public Admin findByUsername(String username) {
        log.info("Finding admin by username: {}", username);
        List<Admin> admins = adminRepository.findByUsername(username);
        if (admins.isEmpty()) {
            return null;
        }
        if (admins.size() > 1) {
            log.warn("Found {} duplicate admins with username '{}'. Using the first one (ID: {}). Please clean up duplicates.", 
                    admins.size(), username, admins.get(0).getId());
        }
        return admins.get(0);
    }

    /**
     * Finds admin by email.
     * @param email The email to search
     * @return Admin if found, null otherwise
     */
    public Admin findByEmail(String email) {
        log.info("Finding admin by email: {}", email);
        List<Admin> admins = adminRepository.findByEmail(email);
        if (admins.isEmpty()) {
            return null;
        }
        if (admins.size() > 1) {
            log.warn("Found {} duplicate admins with email '{}'. Using the first one (ID: {}). Please clean up duplicates.", 
                    admins.size(), email, admins.get(0).getId());
        }
        return admins.get(0);
    }

    /**
     * Finds admin by username or email.
     * @param username The username to search
     * @param email The email to search
     * @return Admin if found, null otherwise
     */
    public Admin findByUsernameOrEmail(String username, String email) {
        log.info("Finding admin by username or email: {}/{}", username, email);
        List<Admin> admins = adminRepository.findByUsernameOrEmail(username, email);
        if (admins.isEmpty()) {
            return null;
        }
        if (admins.size() > 1) {
            log.warn("Found {} duplicate admins with username '{}' or email '{}'. Using the first one (ID: {}). Please clean up duplicates.", 
                    admins.size(), username, email, admins.get(0).getId());
        }
        return admins.get(0);
    }

    /**
     * Checks if admin exists with given username or email.
     * @param username The username to check
     * @param email The email to check
     * @return true if exists, false otherwise
     */
    public boolean existsByUsernameOrEmail(String username, String email) {
        return adminRepository.existsByUsernameOrEmail(username, email);
    }

    // ========== COMMAND METHODS ==========

    /**
     * Saves an admin entity to the database.
     * @param admin The admin to save
     * @return The saved admin with ID
     * @throws CustomException if validation fails
     */
    public Admin save(Admin admin) {
        log.info("Saving admin: {}", admin.getUsername());
        return adminRepository.save(admin);
    }

    /**
     * Deletes an admin by ID.
     * @param id The admin ID to delete
     * @throws CustomException if admin is not found
     */
    public void delete(Long id) {
        // Check if admin exists before deletion
        if (!adminRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Admin not found", HttpStatus.NOT_FOUND);
        }
        log.info("Deleting admin with ID: {}", id);
        adminRepository.deleteById(id);
    }

    // ========== SPRING SECURITY METHODS ==========

    /**
     * Loads user by username for Spring Security authentication.
     * @param username The username to look up
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("User authenticated: {}", username);
        List<Admin> admins = adminRepository.findByUsername(username);
        if (admins.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        if (admins.size() > 1) {
            log.warn("Found {} duplicate admins with username '{}' during authentication. Using the first one (ID: {}). Please clean up duplicates.", 
                    admins.size(), username, admins.get(0).getId());
        }
        return admins.get(0);
    }
}