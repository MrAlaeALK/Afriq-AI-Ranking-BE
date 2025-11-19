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
        Map<Integer, Integer> weightsByYear, // Weight for the given year (1-100)
        Map<Integer, Integer> effectiveWeightsByYear, // Final weight in overall ranking (calculated)
        Map<Integer, Integer> dimensionWeightsByYear, // Weight of the dimension itself in overall ranking (1-100)
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
}
