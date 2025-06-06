package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Upload.SpreadsheetDataDTO;
import com.pfa.pfaproject.dto.Upload.SpreadsheetUploadDTO;
import com.pfa.pfaproject.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ExcelProcessingService {

    private static final int COUNTRY_COL = 0;
    private static final String COUNTRY_HEADER = "Country";

    /**
     * Processes an Excel file and extracts data into SpreadsheetDataDTO objects
     *
     * @param file The uploaded Excel file
     * @param uploadDTO The upload metadata
     * @return List of extracted and validated data
     * @throws CustomException if file processing fails
     */
    public List<SpreadsheetDataDTO> processExcelFile(MultipartFile file, SpreadsheetUploadDTO uploadDTO) {
        validateFile(file, uploadDTO);

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(file, inputStream);
            List<SpreadsheetDataDTO> data = extractDataFromWorkbook(workbook, uploadDTO.year());

            return data;

        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new CustomException("Failed to read Excel file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file, SpreadsheetUploadDTO uploadDTO) {
        if (file.isEmpty()) {
            throw new CustomException("File is empty", HttpStatus.BAD_REQUEST);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new CustomException("File name is required", HttpStatus.BAD_REQUEST);
        }

        // Check file extension
        if (!fileName.toLowerCase().endsWith(".xlsx") &&
                !fileName.toLowerCase().endsWith(".xls") &&
                !fileName.toLowerCase().endsWith(".csv")) {
            throw new CustomException("Invalid file type. Must be .xls, .xlsx, or .csv", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates appropriate workbook based on file extension
     */
    private Workbook createWorkbook(MultipartFile file, InputStream inputStream) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new CustomException("Unsupported file format", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Extracts data from the Excel workbook
     */
    private List<SpreadsheetDataDTO> extractDataFromWorkbook(Workbook workbook, Integer year) {
        Sheet sheet = workbook.getSheetAt(0);
        List<SpreadsheetDataDTO> dataList = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        // Get headers (Indicator names)
        List<String> indicatorNames = extractHeaders(sheet);

        // Process each data row
        int rowNum = 1;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            if (isEmptyRow(row)) {
                log.debug("Skipping empty row {}", rowNum);
                continue;
            }

            try {
                validateDataRow(row, rowNum);
                List<SpreadsheetDataDTO> rowData = extractDataFromRow(row, indicatorNames, year, rowNum);

                // Validate each data point
                for (SpreadsheetDataDTO data : rowData) {
                    String validationError = validateDataPoint(data);
                    if (validationError == null) {
                        dataList.add(data);
                    } else {
                        validationErrors.add(String.format("Row %d: %s", rowNum, validationError));
                        log.warn("Invalid data in row {}: {}", rowNum, validationError);
                    }
                }

            } catch (Exception e) {
                log.error("Error processing row {}: {}", rowNum, e.getMessage());
                validationErrors.add(String.format("Row %d: %s", rowNum, e.getMessage()));
            }

            rowNum++;
        }

        if (dataList.isEmpty()) {
            throw new CustomException("No valid data found in Excel file", HttpStatus.BAD_REQUEST);
        }

        // Log validation errors but continue processing
        if (!validationErrors.isEmpty()) {
            log.warn("Found {} validation errors during processing", validationErrors.size());
        }

        return dataList;
    }

    /**
     * Extracts headers from the first row
     * @return List of indicator names (excluding "Country" column)
     */
    private List<String> extractHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new CustomException("Excel file must have a header row", HttpStatus.BAD_REQUEST);
        }

        List<String> indicatorNames = new ArrayList<>();

        // Validate first column is "Country"
        Cell countryCell = headerRow.getCell(COUNTRY_COL);
        String countryHeader = getCellValueAsString(countryCell);
        if (!COUNTRY_HEADER.equalsIgnoreCase(countryHeader)) {
            throw new CustomException(
                    String.format("First column must be '%s' but found '%s'", COUNTRY_HEADER, countryHeader),
                    HttpStatus.BAD_REQUEST);
        }

        // Extract indicator names from remaining columns
        int lastCellNum = headerRow.getLastCellNum();
        for (int i = 1; i < lastCellNum; i++) { // Start from column 1 (skip Country)
            Cell cell = headerRow.getCell(i);
            String indicatorName = getCellValueAsString(cell);

            if (indicatorName != null && !indicatorName.trim().isEmpty()) {
                indicatorNames.add(indicatorName.trim());
            } else {
                log.warn("Empty indicator name found in column {}", i + 1);
            }
        }

        if (indicatorNames.isEmpty()) {
            throw new CustomException("No indicator columns found in Excel file", HttpStatus.BAD_REQUEST);
        }

        return indicatorNames;
    }

    /**
     * Gets cell value as string, handling different cell types
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * Checks if a row is empty (all cells are null or empty)
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            if (value != null && !value.trim().isEmpty()) {
                return false; // Has some data
            }
        }
        return true;
    }

    /**
     * Validates that a data row has required fields
     */
    private void validateDataRow(Row row, int rowNum) {
        String countryName = getCellValueAsString(row.getCell(COUNTRY_COL));

        // Row has data but missing country name = ERROR
        if (countryName == null || countryName.trim().isEmpty()) {
            if (!isEmptyRow(row)) {
                throw new CustomException(
                        String.format("Row %d: Country name is required when data is present", rowNum),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * Extracts data from a single row (one country, multiple indicators)
     * @return List of SpreadsheetDataDTO (one per indicator in the row)
     */
    private List<SpreadsheetDataDTO> extractDataFromRow(Row row, List<String> indicatorNames,
                                                        Integer year, int rowNum) {
        List<SpreadsheetDataDTO> rowData = new ArrayList<>();

        // Get country name from first column
        String countryName = getCellValueAsString(row.getCell(COUNTRY_COL));
        if (countryName == null || countryName.trim().isEmpty()) {
            log.warn("Empty country name in row {}", rowNum);
            return rowData; // Skip this row
        }

        countryName = countryName.trim();

        // Extract indicator values from remaining columns
        for (int i = 0; i < indicatorNames.size(); i++) {
            int columnIndex = i + 1; // +1 because first column is Country
            Cell valueCell = row.getCell(columnIndex);
            Double value = getCellValueAsDouble(valueCell);

            String indicatorName = indicatorNames.get(i);

            // Create DTO for this country-indicator combination
            SpreadsheetDataDTO dataPoint = SpreadsheetDataDTO.create(
                    countryName,
                    indicatorName,
                    value,
                    year,
                    rowNum
            );

            rowData.add(dataPoint);
        }

        return rowData;
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String stringCellValue = cell.getStringCellValue().trim();
                    return stringCellValue.isEmpty() ? null : Double.parseDouble(stringCellValue);
                case FORMULA:
                    return cell.getNumericCellValue();
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse numeric value from cell: {}", cell.toString());
            return null;
        }
    }

    /**
     * Validates a single data point
     * @param data The data to validate
     * @return Error message if invalid, null if valid
     */
    private String validateDataPoint(SpreadsheetDataDTO data) {
        if (data.countryName() == null || data.countryName().trim().isEmpty()) {
            return "Country name is missing";
        }
        if (data.indicatorName() == null || data.indicatorName().trim().isEmpty()) {
            return "Indicator name is missing";
        }
        if (data.value() == null) {
            return "Value is missing";
        }
        if (data.value().isNaN() || data.value() < 0) {
            return "Value must be a positive number";
        }
        if (data.year() == null || data.year() > java.time.LocalDate.now().getYear()) {
            return "Year must be valid and not in future";
        }
        return null; // No errors
    }
}