package com.pfa.pfaproject.controller;

import com.pfa.pfaproject.dto.Admin.RegisterDTO;
import com.pfa.pfaproject.dto.Rank.GenerateRankOrFinalScoreDTO;
import com.pfa.pfaproject.dto.Score.AddOrUpdateScoreDTO;
import com.pfa.pfaproject.dto.Score.AddScoreDTO;
import com.pfa.pfaproject.dto.Score.ScoreDTO;
import com.pfa.pfaproject.dto.ValidatedScoresDTO;
import com.pfa.pfaproject.dto.WantedColumnsDTO;
import com.pfa.pfaproject.dto.Weight.AddIndicatorWeightDTO;
import com.pfa.pfaproject.dto.Weight.AddWeightDTO;
import com.pfa.pfaproject.dto.YearRequestDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.service.AdminBusinessService;
import com.pfa.pfaproject.service.FastApiService;
import com.pfa.pfaproject.service.DimensionService;
import com.pfa.pfaproject.service.DimensionWeightService;
import com.pfa.pfaproject.service.IndicatorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminActionsController {
    private final AdminBusinessService adminBusinessService;
    private final FastApiService fastApiService;
    private final DimensionService dimensionService;
    private final DimensionWeightService dimensionWeightService;
    private final IndicatorService indicatorService;

    /**
     * Adds a new country to the system.
     * 
     * @param country The country to add
     * @return The created country
     */
    @PostMapping("/country")
    public ResponseEntity<?> addCountry(@Valid @RequestBody Country country) {
        Country addedCountry = adminBusinessService.addCountry(country);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(addedCountry));
    }

    @PostMapping("/countries")
    public ResponseEntity<?> addCountries(@Valid @RequestBody List<Country> countries) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(adminBusinessService.addCountries(countries)));
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
        String result = adminBusinessService.deleteRankingByYear(year);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success(result));
    }

    // Weight validation endpoints
    @PostMapping("/validate-year-weights")
    public ResponseEntity<?> validateYearWeights(@Valid @RequestBody YearRequestDTO request) {
        try {
            Map<String, Object> validationResponse = adminBusinessService.validateWeightsForYear(request.year());
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success(validationResponse));
        } catch (Exception e) {
            // Return validation failure response in expected format
            Map<String, Object> validationResponse = Map.of(
                "canGenerateRanking", false,
                "message", "Erreur lors de la validation: " + e.getMessage(),
                "validationResults", Map.of("status", "error", "error", e.getMessage()),
                "indicatorValidation", Map.of("status", "error"),
                "invalidDimensions", List.of("validation_error"),
                "summary", Map.of("error", e.getMessage())
            );
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success(validationResponse));
        }
    }

    @PostMapping("/validate-dimension-weights")
    public ResponseEntity<?> validateDimensionWeights(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Dimension weights validation not implemented"));
    }

    @GetMapping("/weight-validation-report")
    public ResponseEntity<?> getWeightValidationReport(@RequestParam(required = false) Integer year) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Weight validation report not implemented"));
    }

    @PostMapping("/suggest-weight-adjustment")
    public ResponseEntity<?> suggestWeightAdjustment(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Weight adjustment suggestions not implemented"));
    }

    @PostMapping("/validate-dimension-weights-for-year")
    public ResponseEntity<?> validateDimensionWeightsForYear(@Valid @RequestBody YearRequestDTO request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Dimension weights validation for year not implemented"));
    }

    @PostMapping("/generate-ranking-with-validation")
    public ResponseEntity<?> generateRankingWithValidation(@Valid @RequestBody YearRequestDTO request) {
        try {
            List<Country> ranking = adminBusinessService.generateRanking(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success(ranking));
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @PostMapping("/update-indicator-weights-batch")
    public ResponseEntity<?> updateIndicatorWeightsBatch(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Batch weight update not implemented"));
    }

    @PostMapping("/clear-and-set-equal-weights")
    public ResponseEntity<?> clearAndSetEqualWeights(@RequestBody Map<String, Object> request) {
        try {
            Long dimensionId = ((Number) request.get("dimensionId")).longValue();
            Integer year = ((Number) request.get("year")).intValue();
            
            adminBusinessService.clearAndSetEqualIndicatorWeights(dimensionId, year);
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success("Poids des indicateurs ajustés automatiquement avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Erreur lors de l'ajustement automatique: " + e.getMessage()));
        }
    }

    @PostMapping("/apply-weight-adjustment")
    public ResponseEntity<?> applyWeightAdjustment(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseWrapper.success("Weight adjustment application not implemented"));
    }

    @PostMapping("/fix-weight-totals/{year}")
    public ResponseEntity<?> fixWeightTotals(@PathVariable Integer year) {
        try {
            // Normalize all indicator weights for the year
            List<Dimension> dimensions = dimensionService.findAll();
            
            for (Dimension dimension : dimensions) {
                indicatorService.normalizeWeightsForDimensionYear(dimension.getId(), year);
            }
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success("Weight totals fixed for year " + year + ". All weights now sum to exactly 100%."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Failed to fix weight totals: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze-effective-weights/{year}")
    public ResponseEntity<?> analyzeEffectiveWeights(@PathVariable Integer year) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Get all indicators with weights for this year
            List<Indicator> indicators = indicatorService.findAll().stream()
                    .filter(indicator -> indicator.getWeights().stream()
                            .anyMatch(weight -> weight.getYear().equals(year)))
                    .collect(Collectors.toList());
            
            // Calculate exact effective weights as doubles
            List<Double> exactEffectiveWeights = new ArrayList<>();
            List<String> indicatorNames = new ArrayList<>();
            
            for (Indicator indicator : indicators) {
                IndicatorWeight indicatorWeight = indicator.getWeights().stream()
                        .filter(weight -> weight.getYear().equals(year))
                        .findFirst()
                        .orElse(null);
                
                if (indicatorWeight != null && indicatorWeight.getWeight() != null) {
                    DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(
                            indicator.getDimension().getId(), year);
                    
                    if (dimensionWeight != null && dimensionWeight.getDimensionWeight() != null) {
                        double effectiveWeight = (dimensionWeight.getDimensionWeight().doubleValue() 
                                * indicatorWeight.getWeight().doubleValue()) / 100.0;
                        exactEffectiveWeights.add(effectiveWeight);
                        indicatorNames.add(indicator.getName());
                    }
                }
            }
            
            // Show current situation (with individual rounding)
            int currentTotal = 0;
            List<Map<String, Object>> currentWeights = new ArrayList<>();
            for (int i = 0; i < exactEffectiveWeights.size(); i++) {
                int rounded = (int) Math.round(exactEffectiveWeights.get(i));
                currentTotal += rounded;
                
                Map<String, Object> weightInfo = new HashMap<>();
                weightInfo.put("indicator", indicatorNames.get(i));
                weightInfo.put("exact", Math.round(exactEffectiveWeights.get(i) * 100.0) / 100.0);
                weightInfo.put("rounded", rounded);
                currentWeights.add(weightInfo);
            }
            
            result.put("year", year);
            result.put("currentTotal", currentTotal);
            result.put("currentWeights", currentWeights);
            result.put("issue", currentTotal == 100 ? "No issue - totals to 100%" : 
                    "Issue found - totals to " + currentTotal + "% instead of 100%");
            result.put("explanation", "Individual rounding of effective weights can cause totals != 100%. " +
                    "This is a mathematical limitation when rounding fractional percentages.");
            
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseWrapper.success(result));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error analyzing effective weights: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO adminToRegister) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(adminBusinessService.register(adminToRegister)));
    }

    @PostMapping("/year_dimensions")
    public ResponseEntity<?> getYearDimensions(@Valid @RequestBody YearRequestDTO yearRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseWrapper.success(adminBusinessService.getYearDimensions(yearRequestDTO.year())));
    }
}
