package com.pfa.pfaproject.dto.indicator;

public record GetYearIndicatorsDTO(
        Long id,
        String name,
        String description,
        Integer weight,
        String dimensionName
) {
}
