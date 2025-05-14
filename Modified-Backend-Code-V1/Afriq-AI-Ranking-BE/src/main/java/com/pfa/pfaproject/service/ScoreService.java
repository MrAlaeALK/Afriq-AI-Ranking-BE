package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.ScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing Score entities and score-related calculations.
 * ===========================================================
 * 
 * This service handles all operations related to Score entities including:
 * - CRUD operations for score management
 * - Score normalization across indicators
 * - Weighted score calculations for country rankings
 * - Score validation and business rule enforcement
 * 
 * The service enforces data integrity through validation and uses transactions
 * to ensure consistency across related operations.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final CountryService countryService;
    private final IndicatorService indicatorService;

    public ScoreService(ScoreRepository scoreRepository, CountryService countryService, IndicatorService indicatorService) {
        this.scoreRepository = scoreRepository;
        this.countryService = countryService;
        this.indicatorService = indicatorService;
    }

    // ========== QUERY METHODS ==========

    /**
     * Returns all scores in the system.
     * @return List of all scores
     */
    public List<Score> findAll() {
        log.info("Retrieving all scores");
        return scoreRepository.findAll();
    }

    /**
     * Finds a score by ID.
     * @param id The score ID
     * @return The found score
     * @throws CustomException if score is not found
     */
    public Score findById(Long id) {
        log.info("Finding score with ID: {}", id);
        return scoreRepository.findById(id)
                .orElseThrow(() -> new CustomException("Score not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds a score by country ID, indicator ID, and year.
     * @param countryId The country ID
     * @param indicatorId The indicator ID
     * @param year The year
     * @return The found score
     * @throws CustomException if score is not found
     */
    public Score findByCountryIdAndIndicatorIdAndYear(Long countryId, Long indicatorId, int year) {
        validateYear(year);
        log.info("Finding score for country ID: {}, indicator ID: {}, year: {}", countryId, indicatorId, year);
        Score score = scoreRepository.findByCountry_IdAndIndicator_IdAndYear(countryId, indicatorId, year);
        if (score == null) {
            throw new CustomException(
                    String.format("Score not found for country ID %d, indicator ID %d in year %d", 
                            countryId, indicatorId, year),
                    HttpStatus.NOT_FOUND);
        }
        return score;
    }

    /**
     * Finds a score by year.
     * @param year The year to search
     * @return The found score
     * @throws CustomException if score is not found
     */
    public Score findByYear(int year) {
        validateYear(year);
        log.info("Finding score for year: {}", year);
        Score score = scoreRepository.findByYear(year);
        if (score == null) {
            throw new CustomException("Score not found for year: " + year, HttpStatus.NOT_FOUND);
        }
        return score;
    }

    // ========== COMMAND METHODS ==========

    /**
     * Saves a score entity to the database.
     * @param score The score to save
     * @return The saved score with ID
     * @throws CustomException if validation fails
     */
    @Transactional
    public Score save(Score score) {
        validateScore(score);
        log.info("Saving score for country: {}, indicator: {}, year: {}", 
                score.getCountry() != null ? score.getCountry().getName() : "unknown",
                score.getIndicator() != null ? score.getIndicator().getName() : "unknown",
                score.getYear());
        return scoreRepository.save(score);
    }

    /**
     * Creates a new score with raw value and applies normalization.
     * @param countryId The country ID
     * @param indicatorId The indicator ID
     * @param year The year
     * @param rawValue The raw score value before normalization
     * @return The created score
     */
    @Transactional
    public Score createScore(Long countryId, Long indicatorId, int year, double rawValue) {
        validateYear(year);
        Country country = countryService.findById(countryId);
        Indicator indicator = indicatorService.findById(indicatorId);
        
        // Check if score already exists
        if (scoreRepository.findByCountry_IdAndIndicator_IdAndYear(countryId, indicatorId, year) != null) {
            throw new CustomException(
                    String.format("Score for country %s, indicator %s in year %d already exists", 
                            country.getName(), indicator.getName(), year),
                    HttpStatus.CONFLICT);
        }
        
        Score score = new Score();
        score.setCountry(country);
        score.setIndicator(indicator);
        score.setYear(year);
        score.setRawValue(rawValue);
        
        // Set initial score the same as raw value, normalization will happen later
        score.setScore(rawValue);
        
        Score savedScore = scoreRepository.save(score);
        log.info("Created score for country: {}, indicator: {}, year: {}", 
                country.getName(), indicator.getName(), year);
        
        return savedScore;
    }
    
    /**
     * Updates the raw value of a score and recalculates normalized value.
     * @param scoreId The score ID
     * @param rawValue The new raw value
     * @return The updated score
     */
    @Transactional
    public Score updateRawValue(Long scoreId, double rawValue) {
        Score score = findById(scoreId);
        score.setRawValue(rawValue);
        
        // Initially just copy the raw value, normalization will be done separately
        score.setScore(rawValue);
        
        Score updatedScore = scoreRepository.save(score);
        log.info("Updated raw value for score ID: {}, new value: {}", scoreId, rawValue);
        
        return updatedScore;
    }

    /**
     * Deletes a score by ID.
     * @param id The score ID to delete
     * @throws CustomException if score is not found
     */
    @Transactional
    public void delete(Long id) {
        if (!scoreRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Score not found", HttpStatus.NOT_FOUND);
        }
        log.info("Deleting score with ID: {}", id);
        scoreRepository.deleteById(id);
    }

    // ========== VALIDATION METHODS ==========
    
    /**
     * Validates that a year is valid according to business rules.
     * @param year The year to validate
     * @throws CustomException if year is invalid
     */
    private void validateYear(int year) {
        if (year < 2000) {
            throw new CustomException("Year must be at least 2000", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validates score data before saving.
     * @param score The score to validate
     * @throws CustomException if validation fails
     */
    private void validateScore(Score score) {
        if (score.getCountry() == null) {
            throw new CustomException("Country is required for score", HttpStatus.BAD_REQUEST);
        }
        
        if (score.getIndicator() == null) {
            throw new CustomException("Indicator is required for score", HttpStatus.BAD_REQUEST);
        }
        
        validateYear(score.getYear());
        
        // Check for duplicate country-indicator-year combination
        Score existingScore = scoreRepository.findByCountry_IdAndIndicator_IdAndYear(
                score.getCountry().getId(), score.getIndicator().getId(), score.getYear());
        
        if (existingScore != null && (score.getId() == null || !score.getId().equals(existingScore.getId()))) {
            throw new CustomException(
                    String.format("Score for country %s, indicator %s in year %d already exists", 
                            score.getCountry().getName(), score.getIndicator().getName(), score.getYear()),
                    HttpStatus.CONFLICT);
        }
    }

    // ========== CALCULATION METHODS ==========

    /**
     * Normalizes all scores for a specific indicator and year using min-max normalization.
     * @param indicatorId The indicator ID
     * @param year The year
     * @return List of normalized scores
     */
    @Transactional
    public List<Score> normalizeScores(Long indicatorId, int year) {
        Indicator indicator = indicatorService.findById(indicatorId);
        log.info("Normalizing scores for indicator: {}, year: {}", indicator.getName(), year);
        
        // Get all scores for this indicator and year
        List<Score> scores = scoreRepository.findByIndicator_IdAndYear(indicatorId, year);
        
        if (scores.isEmpty()) {
            log.info("No scores found for normalization");
            return scores;
        }
        
        // Find min and max values
        double minValue = scores.stream()
                .mapToDouble(s -> s.getRawValue() != null ? s.getRawValue() : 0)
                .min()
                .orElse(0);
        
        double maxValue = scores.stream()
                .mapToDouble(s -> s.getRawValue() != null ? s.getRawValue() : 0)
                .max()
                .orElse(100);
        
        // Apply normalization to each score
        if (maxValue > minValue) {
            for (Score score : scores) {
                if (score.getRawValue() != null) {
                    score.normalizeScore(indicator.getNormalizationType(), minValue, maxValue);
                    scoreRepository.save(score);
                }
            }
            log.info("Normalized {} scores for indicator: {}, year: {}", 
                    scores.size(), indicator.getName(), year);
        } else {
            log.warn("Cannot normalize scores: min and max values are equal");
        }
        
        return scores;
    }
    
    /**
     * Calculates the weighted final score for a country in a specific year.
     * This combines all indicator scores with their respective weights.
     * @param countryId The country ID
     * @param year The year
     * @return The calculated final score
     */
    public double calculateFinalScore(Long countryId, int year) {
        Country country = countryService.findById(countryId);
        log.info("Calculating final score for country: {}, year: {}", country.getName(), year);
        
        // Get all scores for this country and year
        List<Score> scores = scoreRepository.findByCountry_IdAndYear(countryId, year);
        
        if (scores.isEmpty()) {
            log.warn("No scores found for country: {}, year: {}", country.getName(), year);
            return 0;
        }
        
        double totalWeightedScore = 0;
        int totalWeight = 0;
        
        for (Score score : scores) {
            Indicator indicator = score.getIndicator();
            int weight = indicator.getWeight();
            
            // Only include indicators with positive weight
            if (weight > 0) {
                totalWeightedScore += score.getScore() * weight;
                totalWeight += weight;
            }
        }
        
        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0;
        log.info("Calculated final score for country: {}, year: {}: {}", 
                country.getName(), year, finalScore);
        
        return finalScore;
    }
    
    /**
     * Gets the score and weight distribution for a country in a specific year.
     * @param countryId The country ID
     * @param year The year
     * @return Map of indicator names to their weighted scores
     */
    public Map<String, Double> getScoreBreakdown(Long countryId, int year) {
        Country country = countryService.findById(countryId);
        log.info("Getting score breakdown for country: {}, year: {}", country.getName(), year);
        
        List<Score> scores = scoreRepository.findByCountry_IdAndYear(countryId, year);
        
        return scores.stream()
                .collect(Collectors.toMap(
                        s -> s.getIndicator().getName(),
                        s -> s.getScore() * s.getIndicator().getWeight()
                ));
    }
}

