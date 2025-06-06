package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.dto.Weight.AddIndicatorWeightDTO;
import com.pfa.pfaproject.dto.Weight.AddWeightDTO;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.service.AdminBusinessService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling administrative actions for the Afriq-AI Ranking system.
 * 
 * These endpoints are responsible for managing core data entities and
 * generating rankings and scores. All endpoints require administrator privileges.
 * 
 * @since 1.0
 * @version 1.1
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminActionsController {
    private final AdminBusinessService adminBusinessService;

    /**
     * Adds a new country to the system.
     * 
     * @param country The country to add
     * @return The created country
     */
    @PostMapping("/countries")
    public ResponseEntity<?> addCountry(@Valid @RequestBody Country country) {
        Country addedCountry = adminBusinessService.addCountry(country);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(addedCountry));
    }

    /**
     * Adds a new indicator to the system.
     * 
     * @param indicator The indicator to add
     * @return The created indicator
     */
    @PostMapping("/indicators")
    public ResponseEntity<?> addIndicator(@Valid @RequestBody Indicator indicator) {
        Indicator addedIndicator = adminBusinessService.addIndicator(indicator);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(addedIndicator));
    }

    /**
     * Adds a new indicator category to the system.
     *
     * @param dimension The indicator to add
     * @return The created indicator category
     */
    @PostMapping("/dimensions")
    public ResponseEntity<?> addDimension(@Valid @RequestBody Dimension dimension) {
        Dimension addDimension = adminBusinessService.addDimension(dimension);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(addDimension));
    }

    /**
     * Adds a new score for a country on a specific indicator.
     * 
     * @param addOrUpdateScoreDTO DTO containing score details
     * @return The created score
     */
    @PostMapping("/scores")
    public ResponseEntity<?> addScore(@Valid @RequestBody AddOrUpdateScoreDTO addOrUpdateScoreDTO) {
        Score score = adminBusinessService.addScore(addOrUpdateScoreDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(score));
    }

    /**
     * Updates an existing score.
     * 
     * @param addOrUpdateScoreDTO DTO containing updated score details
     * @return The updated score
     */
    @PutMapping("/scores")
    public ResponseEntity<?> updateScore(@Valid @RequestBody AddOrUpdateScoreDTO addOrUpdateScoreDTO) {
        Score score = adminBusinessService.updateScore(addOrUpdateScoreDTO);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(score));
    }

    /**
     * Generates a final score for a specific country in a given year.
     * 
     * @param generateFinalScoreForCountryDTO DTO containing country ID and year
     * @return The generated rank entry
     */
    //maybe we need it later but right now just let it like that so it does not throw any error regarding passing the dto
    // if we need it we will simply change back to that damn dto
//    @PostMapping("/final-scores/country")
//    public ResponseEntity<?> generateFinalScoreForCountry(@Valid @RequestBody GenerateFinalScoreForCountryDTO generateFinalScoreForCountryDTO) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ResponseWrapper.success(adminBusinessService.calculateFinalScoreForCountry(generateFinalScoreForCountryDTO)));
//    }

    /**
     * Generates final scores for all countries in a specific year.
     * 
     * @param generateRankOrFinalScoreDTO DTO containing the year
     * @return List of generated rank entries
     */
    @PostMapping("/final-scores")
    public ResponseEntity<?> generateFinalScore(@Valid @RequestBody GenerateRankOrFinalScoreDTO generateRankOrFinalScoreDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.generateFinalScores(generateRankOrFinalScoreDTO)));
    }

    /**
     * Generates rankings for all countries in a specific year.
     * 
     * @param generateRankOrFinalScoreDTO DTO containing the year
     * @return List of countries ordered by rank
     */
    @PostMapping("/rankings")
    public ResponseEntity<?> generateRanking(@Valid @RequestBody GenerateRankOrFinalScoreDTO generateRankOrFinalScoreDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.generateRanking(generateRankOrFinalScoreDTO)));
    }

    @PostMapping("/indicators-weights")
    public ResponseEntity<?> addIndicatorWeight(@RequestBody AddIndicatorWeightDTO addIndicatorWeightDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.addIndicatorWeight(addIndicatorWeightDTO)));
    }

    @PostMapping("/weights")
    public ResponseEntity<?> addWeight(@RequestBody AddWeightDTO addWeightDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.addDimensionWeight(addWeightDTO)));
    }
}
