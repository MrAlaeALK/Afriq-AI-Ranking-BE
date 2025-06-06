package com.pfa.pfaproject.dto.Upload;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for spreadsheet upload metadata
 * Contains information about what data is being uploaded
 */
public record SpreadsheetUploadDTO(
        @NotNull(message = "Year is required")
        Integer year,

        String fileName,

        String fileSize,

        String fileType,

        // Optional: if admin wants to overwrite existing data
        Boolean overwriteExisting
) {

    /**
     * Factory method with default values
     */
    public static SpreadsheetUploadDTO create(Integer year, String fileName,
                                              String fileSize, String fileType, Boolean overwriteExisting) {
        return new SpreadsheetUploadDTO(year, fileName, fileSize, fileType, false);
    }

    /**
     * Validates if the file type is supported
     */
    public boolean isValidFileType() {
        return fileType != null &&
                (fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || // .xlsx
                        fileType.equals("application/vnd.ms-excel") || // .xls
                        fileType.equals("text/csv")); // .csv
    }

    /**
     * Gets file extension from file name
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}

