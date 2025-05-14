package com.pfa.pfaproject.dto.Rank;

import jakarta.validation.constraints.Min;

import java.time.Year;

/**
 * Data Transfer Object for generating rankings or final scores for all countries in a specific year.
 * 
 * This DTO is used in two contexts:
 * - Generating final scores for all countries in a given year via {@code generateFinalScore}
 * - Generating rankings for all countries in a given year via {@code generateRanking}
 * 
 * The year parameter determines which data period is used for the ranking or score calculation.
 * 
 * @see com.pfa.pfaproject.service.AdminBusinessService#generateFinalScore
 * @see com.pfa.pfaproject.service.AdminBusinessService#generateRanking
 * @since 1.0
 * @version 1.1
 */
public record GenerateRankOrFinalScoreDTO(
        @Min(value = 2000, message = "Year must be 2000 or later")
        int year
) {
    /**
     * Validates the DTO when it's constructed.
     * This compact form constructor ensures the year is not in the future.
     */
    public GenerateRankOrFinalScoreDTO {
        if (year > Year.now().getValue()) {
            throw new IllegalArgumentException("Year cannot be in the future");
        }
    }
}
