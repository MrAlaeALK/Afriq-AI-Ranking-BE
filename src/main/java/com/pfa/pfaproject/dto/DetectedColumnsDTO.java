package com.pfa.pfaproject.dto;

import java.util.List;

public record DetectedColumnsDTO(
        List<String> countryColumns,
        List<String> indicatorColumns
) {
}
