package com.pfa.pfaproject.dto.Weight;

public record AddIndicatorWeightDTO(
        Long indicatorId,
        Long categoryId,
        int weight,
        int year
) {
}
