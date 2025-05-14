package com.pfa.pfaproject.dto.Rank;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Year;

/**
 * Data Transfer Object for generating a final score for a specific country in a given year.
 * 
 * This DTO is used in the context of calculating and generating a country's final score
 * for the Afriq'AI ranking system. It contains the necessary parameters to identify
 * a specific country and the year for which the final score should be generated.
 * 
 * @see com.pfa.pfaproject.service.AdminBusinessService#generateFinalScoreForCountry
 * @since 1.0
 * @version 1.1
 */
public record GenerateFinalScoreForCountryDTO(
        @NotNull(message = "Country ID is required")
        Long countryId,
        
        @Min(value = 2000, message = "Year must be 2000 or later")
        int year
) {
    /**
     * Validates the DTO when it's constructed.
     * This compact form constructor ensures all validation rules are checked.
     */
    public GenerateFinalScoreForCountryDTO {
        if (year > Year.now().getValue()) {
            throw new IllegalArgumentException("Year cannot be in the future");
        }
        
        if (countryId != null && countryId <= 0) {
            throw new IllegalArgumentException("Country ID must be a positive number");
        }
    }
}
