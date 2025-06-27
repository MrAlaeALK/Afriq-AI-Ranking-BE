package com.pfa.pfaproject.dto.Score;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Year;

/**
 * Data Transfer Object for creating or updating score entries in the Afriq'AI ranking system.
 * 
 * This DTO is used in two contexts:
 *  -Creating new scores via the {@code addScore} method
 *  -Updating existing scores via the {@code updateScore} method
 * 
 * The combination of countryId, indicatorId, and year uniquely identifies a score record.
 * 
 * @see com.pfa.pfaproject.service.AdminBusinessService#addScore
 * @see com.pfa.pfaproject.service.AdminBusinessService#updateScore
 */
public record AddOrUpdateScoreDTO(
        @NotNull(message = "Please select a country")
        Long countryId,

        @NotNull(message = "Please select an indicator")
        Long indicatorId,
        
        @Min(value = 2020, message = "Year must be 2020 or later")
        Integer year,
        
        @Min(value = 0, message = "Score must be a positive number")
        Double score
) {
    /**
     * Validates the DTO when it's constructed.
     * This compact form constructor ensures all validation rules are checked.
     */
    public AddOrUpdateScoreDTO {
        int currentYear = Year.now().getValue();
        
        if (year != null) {
            if (year > currentYear) {
                throw new IllegalArgumentException("Year cannot be in the future");
            }
            if (year < 2020) {
                throw new IllegalArgumentException("Year must be 2020 or later");
            }
        }
        
        if (countryId != null && countryId <= 0) {
            throw new IllegalArgumentException("Invalid country selection");
        }
        
        if (indicatorId != null && indicatorId <= 0) {
            throw new IllegalArgumentException("Invalid indicator selection");
        }
    }
}
