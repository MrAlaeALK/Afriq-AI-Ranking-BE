package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.service.CountryService;
import com.pfa.pfaproject.service.RankService;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for country-related operations in the Afriq-AI Ranking system.
 * 
 * Provides endpoints for retrieving country information, both as a list and individually.
 * These endpoints are generally accessible to authenticated users for viewing data.
 * 
 * @since 1.0
 * @version 1.1
 */
@RestController
@RequestMapping("/api/v1/countries")
@AllArgsConstructor
@Validated
public class CountryController {
    private final CountryService countryService;
//    private final RankService rankService;

    /**
     * Retrieves all countries in the system.
     * 
     * @return List of all countries
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllCountries() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(countryService.findAll()));
    }

    /**
     * Retrieves countries filtered by region.
     * 
     * @param region The region to filter by
     * @return List of countries in the specified region
     */
    @GetMapping("/region/{region}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCountriesByRegion(@PathVariable String region) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(countryService.findByRegion(region)));
    }

    /**
     * Retrieves a specific country by ID.
     * 
     * @param id The country ID to retrieve
     * @return The country with the specified ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCountryById(@PathVariable @Min(value = 1, message = "Country ID must be positive") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(countryService.findById(id)));
    }

    /**
     * Searches for a country by its code.
     * 
     * @param code The country code (2-3 letter code)
     * @return The country with the specified code
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCountryByCode(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(countryService.findByCode(code)));
    }

}