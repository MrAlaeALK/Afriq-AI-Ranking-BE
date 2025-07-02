package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Dimension.CreateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.UpdateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.DimensionResponseDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.service.DimensionService;
import com.pfa.pfaproject.service.DimensionWeightService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dimension")
@AllArgsConstructor
@Validated
public class DimensionController {
    private final DimensionService dimensionService;
    private final DimensionWeightService dimensionWeightService;

    @GetMapping("/alldimensions")
    public ResponseEntity<?> findAllDimensions() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionService.findAll()));
    }

    @GetMapping("/dimensions")
    public ResponseEntity<?> getAllDimensions() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionService.getAllDimensions()));
    }

    @PostMapping("/year_dimensions")
    public ResponseEntity<?> getYearDimensions(@RequestBody Map<String, Integer> year) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(dimensionWeightService.getYearDimensions(year.get("year"))));
    }

    /**
     * Create a new dimension
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDimension(@Valid @RequestBody CreateDimensionDTO createDimensionDTO) {
        try {
            DimensionResponseDTO createdDimension = dimensionService.createDimension(createDimensionDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseWrapper.success(createdDimension));
        } catch (CustomException e) {
            if (e.getStatus() == HttpStatus.CONFLICT && e.getMessage().contains("invalidera les classements")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ResponseWrapper.error(e.getMessage(), HttpStatus.CONFLICT));
            }
            throw e; // Re-throw other exceptions
        }
    }

    /**
     * Force create a new dimension (bypasses ranking validation)
     */
    @PostMapping("/force-create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceCreateDimension(@Valid @RequestBody CreateDimensionDTO createDimensionDTO) {
        DimensionResponseDTO createdDimension = dimensionService.forceCreateDimension(createDimensionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(createdDimension));
    }

    /**
     * Update an existing dimension
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDimension(@PathVariable Long id, @Valid @RequestBody UpdateDimensionDTO updateDimensionDTO) {
        DimensionResponseDTO updatedDimension = dimensionService.updateDimension(id, updateDimensionDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(updatedDimension));
    }

    /**
     * Delete a dimension by ID (with ranking validation)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDimension(@PathVariable Long id) {
        try {
            dimensionService.deleteDimension(id);
            return ResponseEntity.ok(ResponseWrapper.success("Dimension supprimée avec succès"));
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
     * Force delete a dimension by ID (bypasses ranking validation)
     */
    @DeleteMapping("/force-delete/{id}")
    public ResponseEntity<?> forceDeleteDimension(@PathVariable Long id) {
        dimensionService.forceDeleteDimension(id);
        return ResponseEntity.ok(ResponseWrapper.success("Dimension supprimée avec succès (classements peuvent nécessiter une régénération)"));
    }

    /**
     * Normalizes dimension weights for a specific year to sum to 100%.
     */
    @PostMapping("/normalize-weights/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> normalizeDimensionWeights(@PathVariable Integer year) {
        dimensionWeightService.normalizeDimensionWeightsForYear(year);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Dimension weights normalized successfully for year " + year));
    }

    /**
     * Normalizes dimension weights for all years to sum to 100%.
     */
    @PostMapping("/normalize-all-weights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> normalizeAllDimensionWeights() {
        dimensionWeightService.normalizeAllDimensionWeights();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("All dimension weights normalized successfully"));
    }
}
