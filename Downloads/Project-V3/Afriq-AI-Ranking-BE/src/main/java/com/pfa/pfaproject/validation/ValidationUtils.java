package com.pfa.pfaproject.validation;

import com.pfa.pfaproject.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

/**
 * Utility class containing common validation methods for use across the application.
 * Centralizes validation logic for consistent enforcement of business rules.
 * 
 * @since 1.0
 * @version 1.0
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    // Constants for validation
    public static final int MIN_VALID_YEAR = 2000;
    public static final int MAX_INDICATOR_WEIGHT = 100;
    public static final int MIN_INDICATOR_WEIGHT = 1;
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    /**
     * Validates that a year meets the minimum requirements for the application.
     * @param year The year to validate
     * @param errorMessage Optional custom error message (will use default if null)
     * @throws CustomException if the year is invalid
     */
    public static void validateYear(int year, String errorMessage) {
        if (year < MIN_VALID_YEAR) {
            throw new CustomException(
                    errorMessage != null ? errorMessage : 
                    String.format("Valid year is required (must be at least %d)", MIN_VALID_YEAR),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Validates that a year meets the minimum requirements for the application.
     * Uses default error message.
     * @param year The year to validate
     * @throws CustomException if the year is invalid
     */
    public static void validateYear(int year) {
        validateYear(year, null);
    }
    
    /**
     * Validates that an indicator weight is within acceptable range.
     * @param weight The weight to validate
     * @throws CustomException if the weight is invalid
     */
    public static void validateIndicatorWeight(int weight) {
        if (weight < MIN_INDICATOR_WEIGHT || weight > MAX_INDICATOR_WEIGHT) {
            throw new CustomException(
                    String.format("Weight must be between %d and %d", 
                            MIN_INDICATOR_WEIGHT, MAX_INDICATOR_WEIGHT),
                    HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Checks if a string is a valid email address.
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates that a string is not null or empty.
     * @param value The string to validate
     * @param fieldName The name of the field (for error message)
     * @throws CustomException if the string is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(
                    String.format("%s is required", fieldName),
                    HttpStatus.BAD_REQUEST);
        }
    }
} 