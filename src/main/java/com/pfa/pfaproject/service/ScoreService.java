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
    public Score findByCountryIdAndIndicatorIdAndYear(Long countryId, Long indicatorId, Integer  year) {
        return scoreRepository.findByCountry_IdAndIndicator_IdAndYear(countryId, indicatorId, year);
    }

    public Score findByCountryNameAndIndicatorNameAndYear(String countryName, String indicatorName, Integer  year) {
        return scoreRepository.findByCountry_NameAndIndicator_NameAndYear(countryName, indicatorName, year);
    }


    /**
     * Saves a score entity to the database.
     * @param score The score to save
     * @return The saved score
     */
    public Score save(Score score) {
        score.setScore(Utils.round(score.getScore(), 2));
        return scoreRepository.save(score);
    }

    
    /**
     * Updates the raw value of a score and recalculates normalized value.
     * @param scoreId The score ID
     * @param rawValue The new raw value
     * @return The updated score
     */
    @Transactional
    public Score updateRawValue(Long scoreId, Double rawValue) {
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
}

