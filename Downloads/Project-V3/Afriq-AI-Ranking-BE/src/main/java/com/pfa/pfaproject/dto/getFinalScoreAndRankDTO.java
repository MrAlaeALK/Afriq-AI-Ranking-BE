package com.pfa.pfaproject.dto;

public record getFinalScoreAndRankDTO(
        Long countryId,
        String countryName,
        String countryCode,
        String countryRegion,
        double finalScore,
        int rank
) {
}
