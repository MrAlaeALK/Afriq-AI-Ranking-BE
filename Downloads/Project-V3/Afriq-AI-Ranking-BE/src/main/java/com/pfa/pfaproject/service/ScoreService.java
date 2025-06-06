package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.ScoreRepository;
import com.pfa.pfaproject.util.Utils;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Transactional(readOnly = true)
public class ScoreService {
    private final ScoreRepository scoreRepository;
//    private final CountryService countryService;
//    private final IndicatorService indicatorService;

    /**
     * Returns all scores in the system.
     * @return List of all scores
     */
    public List<Score> findAll() {
        return scoreRepository.findAll();
    }

    /**
     * Finds a score by ID.
     * @param id The score ID
     * @return The found score
     * @throws CustomException if score is not found
     */
    public Score findById(Long id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new CustomException("Score not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds a score by country ID, indicator ID, and year.
     * @param countryId The country ID
     * @param indicatorId The indicator ID
     * @param year The year
     * @return The found score
     */
    public Score findByCountryIdAndIndicatorIdAndYear(Long countryId, Long indicatorId, int year) {
        return scoreRepository.findByCountry_IdAndIndicator_IdAndYear(countryId, indicatorId, year);
    }

    /**
     * Finds a score by year.
     * @param year The year to search
     * @return The found score
     * @throws CustomException if score is not found
     */
//    public Score findByYear(int year) {
//
//        Score score = scoreRepository.findByYear(year);
//        if (score == null) {
//            throw new CustomException("Score not found for year: " + year, HttpStatus.NOT_FOUND);
//        }
//        return score;
//    }

    /**
     * Saves a score entity to the database.
     * @param score The score to save
     * @return The saved score
     */
    public Score save(Score score) {
//        score.setRawValue(Utils.round(score.getRawValue(), 2));
        score.setScore(Utils.round(score.getScore(), 2));
        return scoreRepository.save(score);
    }

    /**
     * Creates a new score with raw value.
     * @param countryId The country ID
     * @param indicatorId The indicator ID
     * @param year The year
     * @param rawValue The raw score value before normalization
     * @return The created score
     */
//    public Score createScore(Long countryId, Long indicatorId, int year, double rawValue) {
//
//        Country country = countryService.findById(countryId);
//        Indicator indicator = indicatorService.findById(indicatorId);
//
//        // Check if score already exists
//        if (scoreRepository.findByCountry_IdAndIndicator_IdAndYear(countryId, indicatorId, year) != null) {
//            throw new CustomException(
//                    String.format("Score for country %s, indicator %s in year %d already exists",
//                            country.getName(), indicator.getName(), year),
//                    HttpStatus.CONFLICT);
//        }
//
//        Score score = new Score();
//        score.setCountry(country);
//        score.setIndicator(indicator);
//        score.setYear(year);
//        score.setRawValue(rawValue);
//
//        // Set initial score the same as raw value, normalization will happen later
//        score.setScore(rawValue);
//
//        Score savedScore = scoreRepository.save(score);
//
//        return savedScore;
//    }
    
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
        scoreRepository.deleteById(id);
    }

    // Todo: create a normalisation for the raw values
    
    /**
     * Calculates the weighted final score for a country in a specific year.
     * This combines all indicator scores with their respective weights.
     * @param countryId The country ID
     * @param year The year
     * @return The calculated final score
     */
//    public double calculateFinalScore(Long countryId, int year) {
//        Country country = countryService.findById(countryId);
//
//        // Get all scores for this country and year
//        List<Score> scores = scoreRepository.findByCountry_IdAndYear(countryId, year);
//
//        if (scores.isEmpty()) {
//            log.warn("No scores found for country: {}, year: {}", country.getName(), year);
//            return 0;
//        }
//
//        double totalWeightedScore = 0;
//        int totalWeight = 0;
//
//        for (Score score : scores) {
//            Indicator indicator = score.getIndicator();
//            int weight = indicator.getWeights()
//                    .stream()
//                    .filter(indicatorWeight -> indicatorWeight.getYear() == year)
//                    .findFirst().get().getWeight();
//
//            // Only include indicators with positive weight
//            if (weight > 0) {
//                totalWeightedScore += score.getScore() * weight;
//                totalWeight += weight;
//            }
//        }
//
//        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0;
//
//        return finalScore;
//    }
}

