package com.pfa.pfaproject.dto.Dimension;

public record DimensionResponseDTO(
        Long id,
        String name,
        String description,
        Integer weight,
        Integer year
) {
} 