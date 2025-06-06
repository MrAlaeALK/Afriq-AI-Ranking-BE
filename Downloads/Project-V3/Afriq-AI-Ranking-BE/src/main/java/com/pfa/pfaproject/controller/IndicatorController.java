package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.service.IndicatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for indicator-related operations in the Afriq-AI Ranking system.
 * 
 * Provides endpoints for retrieving and managing indicators, which are the metrics
 * used to evaluate and score countries in the ranking system.
 * 
 * @since 1.0
 * @version 1.1
 */
@RestController
@RequestMapping("/api/v1/indicators")
@AllArgsConstructor
@Validated
public class IndicatorController {
    private final IndicatorService indicatorService;

    /**
     * Retrieves all indicators in the system.
     * 
     * @return List of all indicators
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllIndicators() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(indicatorService.findAll()));
    }

    /**
     * Retrieves a specific indicator by ID.
     * 
     * @param id The indicator ID to retrieve
     * @return The indicator with the specified ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getIndicatorById(@PathVariable @Min(value = 1, message = "Indicator ID must be positive") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(indicatorService.findById(id)));
    }
    
    /**
     * Retrieves indicators filtered by category.
     * 
     * @param categoryId The category ID to filter by
     * @return List of indicators in the specified category
     */
//    @GetMapping("/category/{categoryId}")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<?> getIndicatorsByCategory(
//            @PathVariable @Min(value = 1, message = "Category ID must be positive") Long categoryId) {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseWrapper.success(indicatorService.findByDimension(categoryId)));
//    }
}
