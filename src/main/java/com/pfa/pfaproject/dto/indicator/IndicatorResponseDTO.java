package com.pfa.pfaproject.dto.indicator;


import com.pfa.pfaproject.dto.Dimension.GetDimensionsDTO;
import com.pfa.pfaproject.model.Indicator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record IndicatorResponseDTO(
        Long id,
        String name,
        String description,
        String normalizationType,
        List<Integer> availableYears,
        GetDimensionsDTO dimension,
        Map<Integer, Double> weightsByYear, // Initial weight for the given year
        Map<Integer, Double> effectiveWeightsByYear, // Final weight in overall ranking (dimension × indicator weight)
        Map<Integer, Double> dimensionWeightsByYear, // Weight of the dimension itself in overall ranking (0.0-1.0)
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
}
