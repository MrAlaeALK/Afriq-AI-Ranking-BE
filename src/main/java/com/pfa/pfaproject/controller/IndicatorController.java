package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.indicator.CreateIndicatorDTO;
import com.pfa.pfaproject.dto.indicator.UpdateIndicatorDTO;
import com.pfa.pfaproject.dto.indicator.IndicatorResponseDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for indicator-related operations in the Afriq-AI Ranking system.
 *
 * Provides endpoints for retrieving and managing indicators, which are the metrics
 * used to evaluate and score countries in the ranking system.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard/indicators")
@AllArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class IndicatorController {
    private final IndicatorService indicatorService;

    /**
     * Retrieves all indicators in the system.
     */
    @GetMapping
    public ResponseEntity<?> getAllIndicators() {
        List<IndicatorResponseDTO> indicators = indicatorService.findAll()
                .stream()
                .filter(indicator -> indicator.getDimension() != null)
                .map(indicatorService::mapToResponseDTO)
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(indicators));
    }

    /**
     * Retrieves a specific indicator by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getIndicatorById(@PathVariable Long id) {
        IndicatorResponseDTO indicator = indicatorService.findByIdWithDetails(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(indicator));
    }

    /**
     * Creates a new indicator.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createIndicator(@Valid @RequestBody CreateIndicatorDTO createIndicatorDTO) {
        try {
            IndicatorResponseDTO createdIndicator = indicatorService.createIndicator(createIndicatorDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseWrapper.success(createdIndicator));
        } catch (CustomException e) {
            if (e.getStatus() == HttpStatus.CONFLICT && e.getMessage().contains("invalidera les classements")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseWrapper.error(e.getMessage(), HttpStatus.CONFLICT));
            }
            throw e; // Re-throw other exceptions
        }
    }

    /**
     * Force creates a new indicator (bypasses ranking validation).
     */
    @PostMapping("/force-create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceCreateIndicator(@Valid @RequestBody CreateIndicatorDTO createIndicatorDTO) {
        IndicatorResponseDTO createdIndicator = indicatorService.forceCreateIndicator(createIndicatorDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(createdIndicator));
    }

    /**
     * Updates an existing indicator.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateIndicator(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIndicatorDTO updateIndicatorDTO) {
        IndicatorResponseDTO updatedIndicator = indicatorService.updateIndicator(id, updateIndicatorDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(updatedIndicator));
    }

    /**
     * Deletes an indicator by ID (with ranking validation).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteIndicator(@PathVariable Long id) {
        try {
            indicatorService.deleteIndicator(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success("Indicateur supprimé avec succès"));
        } catch (CustomException e) {
            if (e.getMessage().startsWith("RANKING_EXISTS_WARNING:")) {
                // Extract the warning message after the prefix
                String warningMessage = e.getMessage().substring("RANKING_EXISTS_WARNING:".length());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseWrapper.error(warningMessage, HttpStatus.CONFLICT));
            }
            throw e; // Re-throw other exceptions
        }
    }

    /**
     * Force deletes an indicator by ID (bypasses ranking validation).
     */
    @DeleteMapping("/force/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceDeleteIndicator(@PathVariable Long id) {
        indicatorService.forceDeleteIndicator(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Indicateur supprimé avec succès (classements peuvent nécessiter une régénération)"));
    }

    /**
     * Retrieves indicators by dimension ID.
     */
    @GetMapping("/dimension/{dimensionId}")
    public ResponseEntity<?> getIndicatorsByDimension(@PathVariable Long dimensionId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(indicatorService.findByDimensionId(dimensionId)));
    }

    /**
     * Retrieves weight of an indicator for a specific year.
     */
    @GetMapping("/{id}/weight/{year}")
    public ResponseEntity<?> getIndicatorWeightByYear(@PathVariable Long id, @PathVariable Integer year) {
        Integer weight = indicatorService.getWeightByYear(id, year);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(weight));
    }

    /**
     * Bulk delete indicators.
     */
    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkDeleteIndicators(@RequestBody List<Long> indicatorIds) {
        indicatorService.bulkDeleteIndicators(indicatorIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Bulk delete completed successfully"));
    }

    /**
     * Normalizes weights for a specific dimension and year to sum to 100%.
     */
    @PostMapping("/normalize/{dimensionId}/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> normalizeWeights(@PathVariable Long dimensionId, @PathVariable Integer year) {
        indicatorService.normalizeWeightsForDimensionYear(dimensionId, year);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Weights normalized successfully for dimension " + dimensionId + " year " + year));
    }

    /**
     * Normalizes all weights across all dimensions and years to sum to 100%.
     */
    @PostMapping("/normalize-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> normalizeAllWeights() {
        indicatorService.normalizeAllWeights();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("All weights normalized successfully"));
    }

    /**
     * Gets the current weight total for a dimension and year.
     */
    @GetMapping("/dimension/{dimensionId}/year/{year}/weight-total")
    public ResponseEntity<?> getDimensionWeightTotal(@PathVariable Long dimensionId, @PathVariable Integer year) {
        int weightTotal = indicatorService.getDimensionWeightTotal(dimensionId, year);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(Map.of(
                    "dimensionId", dimensionId,
                    "year", year,
                    "weightTotal", weightTotal,
                    "percentageTotal", weightTotal, // Already in percentage (1-100)
                    "remaining", Math.max(0, 100 - weightTotal)
                )));
    }
}