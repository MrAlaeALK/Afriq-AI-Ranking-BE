package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Indicator entities and their relationships.<br/><br/>
 * 
 * This service handles all operations related to Indicator entities including:
 * - CRUD operations for indicator management
 * - Relationship management with categories and scores
 * - Business validation rules enforcement
 * 
 * Indicators are a core part of the ranking system, representing the metrics
 * by which countries are evaluated and scored.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@Data
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IndicatorService {
    private final IndicatorRepository indicatorRepository;
    
    // Default normalization type constant
    private static final String DEFAULT_NORMALIZATION_TYPE = "MinMax Normalisation";

    /**
     * Returns all indicators in the system.
     * @return List of all indicators
     */
    public List<Indicator> findAll() {
        return indicatorRepository.findAll();
    }

    /**
     * Finds an indicator by ID.
     * @param id The indicator ID
     * @return The found indicator
     * @throws CustomException if indicator is not found
     */
    public Indicator findById(Long id) {
        return indicatorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Indicator not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds an indicator by name.
     * @param name The indicator name to search
     * @return The found indicator
     * @throws CustomException if indicator is not found
     */
    public Indicator findByName(String name) {
        return indicatorRepository.findByName(name);
    }

    /**
     * Saves an indicator entity to the database.
     * Uses MinMax Normalisation as default if normalizationType is not specified.
     * @param indicator The indicator to save
     * @return The saved indicator with ID
     * @throws CustomException if validation fails
     */
    public Indicator save(Indicator indicator) {
        
        // Set default normalization type if not specified
        if (indicator.getNormalizationType() == null) {
            indicator.setNormalizationType(DEFAULT_NORMALIZATION_TYPE);
        }

        return indicatorRepository.save(indicator);
    }

    /**
     * Deletes an indicator by ID.
     * @param id The indicator ID to delete
     * @throws CustomException if indicator is not found
     */
    public void delete(Long id) {
        if (!indicatorRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Indicator not found", HttpStatus.NOT_FOUND);
        }
        indicatorRepository.deleteById(id);
    }


    /**
     * Adds a score to an indicator.
     * @param indicatorId The indicator ID
     * @param score The score to add
     * @return The updated indicator
     */
    @Transactional
    public Indicator addScore(Long indicatorId, Score score) {
        Indicator indicator = findById(indicatorId);
        indicator.addScore(score);
        return indicatorRepository.save(indicator);
    }

    /**
     * Removes a score from an indicator.
     * @param indicatorId The indicator ID
     * @param score The score to remove
     * @return The updated indicator
     */
    @Transactional
    public Indicator removeScore(Long indicatorId, Score score) {
        Indicator indicator = findById(indicatorId);
        indicator.removeScore(score);
        return indicatorRepository.save(indicator);
    }
}

