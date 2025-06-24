package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.dto.Score.AddScoreDTO;
import com.pfa.pfaproject.dto.Score.ScoreDTO;
import com.pfa.pfaproject.dto.ValidatedScoresDTO;
import com.pfa.pfaproject.dto.WantedColumnsDTO;
import com.pfa.pfaproject.dto.Weight.AddIndicatorWeightDTO;
import com.pfa.pfaproject.dto.Weight.AddWeightDTO;
import com.pfa.pfaproject.dto.YearRequestDTO;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.service.AdminBusinessService;
import com.pfa.pfaproject.service.FastApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
    private final FastApiService fastApiService;

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
    public ResponseEntity<?> addCategory(@Valid @RequestBody Dimension dimension) {
        Dimension addDimension = adminBusinessService.addDimension(dimension);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(addDimension));
    }

    /**
     * Updates an existing score.
     * 
     * @param addOrUpdateScoreDTO DTO containing updated score details
     * @return The updated score
     */
    @PutMapping("/update-score") //was before scores same as add
    public ResponseEntity<?> updateScore(@Valid @RequestBody AddOrUpdateScoreDTO addOrUpdateScoreDTO) {
        Score score = adminBusinessService.updateScore(addOrUpdateScoreDTO);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success(score));
    }


    /**
     * Generates final scores for all countries in a specific year.
     * 
     * @param generateRankOrFinalScoreDTO DTO containing the year
     * @return List of generated rank entries
     */
    @PostMapping("/final-scores")
    public ResponseEntity<?> generateFinalScore(@Valid @RequestBody YearRequestDTO generateRankOrFinalScoreDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.generateFinalScores(generateRankOrFinalScoreDTO)));
    }

    /**
     * Generates rankings for all countries in a specific year.
     * 
     * @param generateRankOrFinalScoreDTO DTO containing the year
     * @return List of countries ordered by rank
     */
    @PostMapping("/generate-ranking")
    public ResponseEntity<?> generateRanking(@Valid @RequestBody YearRequestDTO generateRankOrFinalScoreDTO) {
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

    //endpoints for scores page
    @GetMapping("/scores")
    public ResponseEntity<?> getAllScores() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getAllScores()));
    }

    @DeleteMapping("/delete-score/{id}")
    public ResponseEntity<?> deleteScore(@PathVariable @NotNull Long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.deleteScore(id)));
    }

    @PostMapping("/edit-score")
    public ResponseEntity<?> updateScore(@Valid @RequestBody ScoreDTO scoreDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.updateScore(scoreDTO)));
    }

    //this is for getting possible years (indicator years) to add scores for
    @GetMapping("/indicators-years")
    public ResponseEntity<?> getAllIndicatorYears() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getAllIndicatorYears()));
    }

    @PostMapping("/year_indicators")
    public ResponseEntity<?> getYearIndicators(@Valid @RequestBody YearRequestDTO yearRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getYearIndicators(yearRequestDTO.year())));
    }

    @GetMapping("/all_countries")
    public ResponseEntity<?> getAllCountries() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getAllCountries()));
    }

    @PostMapping("/add-score")
    public ResponseEntity<?> addScore(@Valid @RequestBody AddScoreDTO addScoreDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.addScore(addScoreDTO)));
    }

    @PostMapping("/upload-score-file")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file){
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(fastApiService.sendFileToFastApi(file)));
    }

    @PostMapping("/validate_fetched_columns")
    public ResponseEntity<?> processConfirmed(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("columns") WantedColumnsDTO dto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(fastApiService.sendJsonToFastApi(file,dto)));
    }

    @PostMapping("/validate_scores")
    public ResponseEntity<?> validateScores(@Valid @RequestBody ValidatedScoresDTO validatedScoresDTO) {
        adminBusinessService.validateScores(validatedScoresDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("scores persisted successfully"));
    }

    @PostMapping("/year-ranking")
    public ResponseEntity<?> getYearRanking(@Valid @RequestBody YearRequestDTO request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getYearRanking(request.year())));
    }

    @GetMapping("/ranking-years")
    public ResponseEntity<?> getAllRankingYears() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getYearsWithRanking()));
    }

    @GetMapping("/all-rankings")
    public ResponseEntity<?> getAllRankings() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getAllRanks()));
    }

    @PostMapping("/dimension-scores")
    public ResponseEntity<?> getDimensionScoresByYear(@Valid @RequestBody YearRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getDimensionScoresByYear(request.year())));
    }

    @DeleteMapping("/delete-ranking/{year}")
    public ResponseEntity<?> deleteRanking(@PathVariable @NotNull Integer year) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.deleteRankingByYear(year)));
    }
}
