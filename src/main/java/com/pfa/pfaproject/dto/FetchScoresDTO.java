package com.pfa.pfaproject.dto;

import java.util.List;

public record FetchScoresDTO(
        String countryColumn,
        List<FetchScoresDTO.IndicatorColumn> indicatorColumns
) {
    public record IndicatorColumn(
            String columnName,
            String indicatorId,
            String normalizationType
    ) {
    }
}
