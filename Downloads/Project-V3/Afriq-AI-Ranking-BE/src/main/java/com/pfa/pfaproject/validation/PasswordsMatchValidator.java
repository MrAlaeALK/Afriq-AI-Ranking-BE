package com.pfa.pfaproject.validation;

import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the PasswordsMatch constraint.
 * Checks if password and passwordConfirmation fields match.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, RegisterDTO> {

    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(RegisterDTO registerDTO, ConstraintValidatorContext context) {
        if (registerDTO == null) {
            return true; // Let @NotNull handle null checks
        }
        
        boolean passwordsMatch = registerDTO.password() != null && 
                                 registerDTO.password().equals(registerDTO.passwordConfirmation());
        
        if (!passwordsMatch) {
            // Disable default message
            context.disableDefaultConstraintViolation();
            
            // Add custom error message
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                   .addPropertyNode("passwordConfirmation")
                   .addConstraintViolation();
        }
        
        return passwordsMatch;
    }
} 