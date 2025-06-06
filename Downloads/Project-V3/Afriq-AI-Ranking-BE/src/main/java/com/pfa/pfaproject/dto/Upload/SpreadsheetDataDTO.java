package com.pfa.pfaproject.dto.Upload;

import com.pfa.pfaproject.model.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO representing a single row of data extracted from the spreadsheet
 * Each instance represents one indicator value for one country
 */
public record SpreadsheetDataDTO(

        @NotBlank(message = "Country name is required")
        String countryName,

        @NotBlank(message = "Indicator name is required")
        String indicatorName,

        @NotBlank(message = "Value name is required")
        Double value,

        @NotNull(message = "Year is required")
        Integer year,

        // row number for error reporting
        Integer rowNumber
) {
    /**
     * Factory method for creating from spreadsheet row
     */
    public static SpreadsheetDataDTO create(String countryName, String indicatorName,
                                            Double value, Integer year, Integer rowNumber) {
        return new SpreadsheetDataDTO(
                countryName != null ? countryName.trim() : null,
                indicatorName != null ? indicatorName.trim() : null,
                value,
                year,
                rowNumber
        );
    }

    
}
