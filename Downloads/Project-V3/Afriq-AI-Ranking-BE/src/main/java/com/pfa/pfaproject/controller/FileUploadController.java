package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Upload.SpreadsheetDataDTO;
import com.pfa.pfaproject.dto.Upload.SpreadsheetUploadDTO;
import com.pfa.pfaproject.service.AdminBusinessService;
import com.pfa.pfaproject.service.ExcelProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.pfa.pfaproject.controller.ResponseWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling Excel file uploads and processing
 * for admin users to bulk update country indicator data
 */
@RestController
@RequestMapping("/api/v1/admin/upload")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class FileUploadController {

    private final ExcelProcessingService excelProcessingService;
    private final AdminBusinessService adminBusinessService;

    /**
     * Validates an uploaded Excel file and returns preview of data
     * This endpoint allows admins to see what data will be imported before confirming
     *
     * @param file The Excel file to validate
     * @param year The year this data represents
     * @param overwriteExisting Whether to overwrite existing data
     * @return Preview of the data that would be imported
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSpreadsheet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year,
            @RequestParam(value = "overwriteExisting", defaultValue = "false") Boolean overwriteExisting) {

        try {

            // Create upload DTO
            SpreadsheetUploadDTO uploadDTO = SpreadsheetUploadDTO.create(
                    year,
                    file.getOriginalFilename(),
                    String.valueOf(file.getSize()),
                    file.getContentType(),
                    overwriteExisting
            );

            // Process and validate the Excel file
            List<SpreadsheetDataDTO> extractedData = excelProcessingService.processExcelFile(file, uploadDTO);

            // Group data for preview
            Map<String, Object> preview = createDataPreview(extractedData);
            preview.put("totalRecords", extractedData.size());
            preview.put("year", year);
            preview.put("fileName", file.getOriginalFilename());
            preview.put("message", "File validated successfully. Review the data before processing.");

            return ResponseEntity.ok(
                    ResponseWrapper.success(preview)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ResponseWrapper.error("Validation failed: " + e.getMessage(), HttpStatus.BAD_REQUEST)
            );
        }
    }

    /**
     * Processes the validated Excel file and saves data to database
     * This endpoint actually imports the data and recalculates rankings
     *
     * @param file The Excel file to process
     * @param year The year this data represents
     * @param overwriteExisting Whether to overwrite existing data
     * @return Success message with processing results
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processSpreadsheet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year,
            @RequestParam(value = "overwriteExisting", defaultValue = "false") Boolean overwriteExisting) {

        try {
            // Create upload DTO
            SpreadsheetUploadDTO uploadDTO = SpreadsheetUploadDTO.create(
                    year,
                    file.getOriginalFilename(),
                    String.valueOf(file.getSize()),
                    file.getContentType(),
                    overwriteExisting
            );

            // Process the Excel file
            List<SpreadsheetDataDTO> extractedData = excelProcessingService.processExcelFile(file, uploadDTO);

            // Save data to database
            Map<String, Object> processingResults = adminBusinessService.processBulkIndicatorData(
                    extractedData,
                    overwriteExisting
            );

            // Only recalculate if we had some successful processing
            int successCount = (Integer) processingResults.get("successCount");
            if (successCount > 0) {
                try {
                    adminBusinessService.recalculateRankingsForYear(year);
                } catch (Exception e) {
                    log.warn("Rankings recalculation failed: {}", e.getMessage());
                    processingResults.put("rankingRecalculationError", e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("year", year);
            response.put("totalProcessed", extractedData.size());
            response.put("processingResults", processingResults);

            // Determine message based on results
            if (successCount == extractedData.size()) {
                response.put("message", "All data processed successfully and rankings updated");
            } else if (successCount > 0) {
                response.put("message", String.format("Partially successful: %d/%d records processed",
                        successCount, extractedData.size()));
            } else {
                response.put("message", "Processing failed for all records");
            }

            return ResponseEntity.ok(ResponseWrapper.success(response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseWrapper.error("Processing failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    /**
     * Gets the upload status and history
     * @return List of recent uploads
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getUploadHistory() {
        try {
            // TODO:
            // This would typically fetch from a database table tracking uploads
            // For now, return a simple message
            Map<String, Object> info = new HashMap<>();
            info.put("message", "Upload history feature coming soon");

            return ResponseEntity.ok(
                    ResponseWrapper.success(List.of(info))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ResponseWrapper.error("Failed to retrieve upload history", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    /**
     * Creates a preview of the data for validation purposes
     * @param extractedData The data extracted from Excel
     * @return Map containing preview information
     */
    private Map<String, Object> createDataPreview(List<SpreadsheetDataDTO> extractedData) {
        Map<String, Object> preview = new HashMap<>();

        // Get unique countries
        List<String> countries = extractedData.stream()
                .map(SpreadsheetDataDTO::countryName)
                .distinct()
                .sorted()
                .toList();

        // Get unique indicators
        List<String> indicators = extractedData.stream()
                .map(SpreadsheetDataDTO::indicatorName)
                .distinct()
                .sorted()
                .toList();

        // Sample of data (first 10 records)
        List<SpreadsheetDataDTO> sampleData = extractedData.stream()
                .limit(10)
                .toList();

        preview.put("countries", countries);
        preview.put("indicators", indicators);
        preview.put("sampleData", sampleData);
        preview.put("countryCount", countries.size());
        preview.put("indicatorCount", indicators.size());

        return preview;
    }
}