package com.pfa.pfaproject.validation;

import com.pfa.pfaproject.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtils to ensure all validation methods
 * function correctly according to business rules.
 */
class ValidationUtilsTest {

    @Test
    void validateYear_withValidYear_shouldNotThrowException() {
        // Given: A valid year (2023)
        int validYear = 2023;
        
        // When/Then: No exception should be thrown
        assertDoesNotThrow(() -> ValidationUtils.validateYear(validYear));
    }
    
    @Test
    void validateYear_withInvalidYear_shouldThrowException() {
        // Given: An invalid year (1999)
        int invalidYear = 1999;
        
        // When/Then: CustomException should be thrown with BAD_REQUEST status
        CustomException exception = assertThrows(CustomException.class, 
                () -> ValidationUtils.validateYear(invalidYear));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Valid year is required"));
    }
    
    @Test
    void validateYear_withCustomMessage_shouldUseProvidedMessage() {
        // Given: An invalid year and custom message
        int invalidYear = 1999;
        String customMessage = "Year must be 2000 or later";
        
        // When/Then: Exception should use the custom message
        CustomException exception = assertThrows(CustomException.class, 
                () -> ValidationUtils.validateYear(invalidYear, customMessage));
        
        assertEquals(customMessage, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
    
    @Test
    void validateIndicatorWeight_withValidWeight_shouldNotThrowException() {
        // Given: Valid weights
        int minWeight = ValidationUtils.MIN_INDICATOR_WEIGHT;
        int maxWeight = ValidationUtils.MAX_INDICATOR_WEIGHT;
        int middleWeight = 50;
        
        // When/Then: No exceptions for valid weights
        assertDoesNotThrow(() -> ValidationUtils.validateIndicatorWeight(minWeight));
        assertDoesNotThrow(() -> ValidationUtils.validateIndicatorWeight(maxWeight));
        assertDoesNotThrow(() -> ValidationUtils.validateIndicatorWeight(middleWeight));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101, 150})
    void validateIndicatorWeight_withInvalidWeight_shouldThrowException(int invalidWeight) {
        // When/Then: Exception for weights outside valid range
        CustomException exception = assertThrows(CustomException.class, 
                () -> ValidationUtils.validateIndicatorWeight(invalidWeight));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("Weight must be between"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user.name@domain.co.uk", "name+tag@example.org"})
    void isValidEmail_withValidEmails_shouldReturnTrue(String validEmail) {
        // When/Then: Valid emails should return true
        assertTrue(ValidationUtils.isValidEmail(validEmail));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "plaintext", "missing@dot", "@nodomain.com", "user@.com"})
    void isValidEmail_withInvalidEmails_shouldReturnFalse(String invalidEmail) {
        // When/Then: Invalid emails should return false
        assertFalse(ValidationUtils.isValidEmail(invalidEmail));
    }
    
    @Test
    void isValidEmail_withNullEmail_shouldReturnFalse() {
        // When/Then: Null email should return false
        assertFalse(ValidationUtils.isValidEmail(null));
    }
    
    @Test
    void validateNotEmpty_withValidString_shouldNotThrowException() {
        // Given: Valid non-empty strings
        String validString = "Test string";
        String fieldName = "Test field";
        
        // When/Then: No exception for non-empty string
        assertDoesNotThrow(() -> ValidationUtils.validateNotEmpty(validString, fieldName));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t", "\n"})
    void validateNotEmpty_withEmptyOrBlankString_shouldThrowException(String emptyString) {
        // Given: A field name
        String fieldName = "Test field";
        
        // When/Then: Exception for empty or blank strings
        CustomException exception = assertThrows(CustomException.class, 
                () -> ValidationUtils.validateNotEmpty(emptyString, fieldName));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(fieldName + " is required", exception.getMessage());
    }
    
    @Test
    void validateNotEmpty_withNullString_shouldThrowException() {
        // Given: Null string and field name
        String nullString = null;
        String fieldName = "Test field";
        
        // When/Then: Exception for null string
        CustomException exception = assertThrows(CustomException.class, 
                () -> ValidationUtils.validateNotEmpty(nullString, fieldName));
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(fieldName + " is required", exception.getMessage());
    }
} 