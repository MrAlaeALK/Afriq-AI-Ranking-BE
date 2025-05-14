package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.IndicatorCategory;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.IndicatorRepository;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Indicator entities and their relationships.
 * ===========================================================
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
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IndicatorService {
    private final IndicatorRepository indicatorRepository;
    
    // Default normalization type constant
    private static final String DEFAULT_NORMALIZATION_TYPE = "MinMax Normalisation";

    // ========== QUERY METHODS ==========

    /**
     * Returns all indicators in the system.
     * @return List of all indicators
     */
    public List<Indicator> findAll() {
        log.info("Retrieving all indicators");
        return indicatorRepository.findAll();
    }

    /**
     * Finds an indicator by ID.
     * @param id The indicator ID
     * @return The found indicator
     * @throws CustomException if indicator is not found
     */
    public Indicator findById(Long id) {
        log.info("Finding indicator with ID: {}", id);
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
        ValidationUtils.validateNotEmpty(name, "Indicator name");
        log.info("Finding indicator with name: {}", name);
        Indicator indicator = indicatorRepository.findByName(name);
        if (indicator == null) {
            throw new CustomException("Indicator not found with name: " + name, HttpStatus.NOT_FOUND);
        }
        return indicator;
    }

    /**
     * Finds all indicators with a specific normalization type.
     * @param normalizationType The normalization type to search
     * @return List of indicators with the specified normalization type
     */
    public List<Indicator> findByNormalizationType(String normalizationType) {
        ValidationUtils.validateNotEmpty(normalizationType, "Normalization type");
        log.info("Finding indicators with normalization type: {}", normalizationType);
        List<Indicator> indicators = indicatorRepository.findByNormalizationType(normalizationType);
        if (indicators.isEmpty()) {
            log.info("No indicators found with normalization type: {}", normalizationType);
        }
        return indicators;
    }

    /**
     * Finds all indicators in a specific category.
     * @param categoryId The category ID to search
     * @return List of indicators in the category
     */
    public List<Indicator> findByCategory(Long categoryId) {
        log.info("Finding indicators in category with ID: {}", categoryId);
        List<Indicator> indicators = indicatorRepository.findByCategory_Id(categoryId);
        if (indicators.isEmpty()) {
            log.info("No indicators found in category with ID: {}", categoryId);
        }
        return indicators;
    }

    /**
     * Checks if an indicator exists by ID.
     * @param id The indicator ID to check
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return indicatorRepository.existsById(id);
    }

    // ========== COMMAND METHODS ==========

    /**
     * Saves an indicator entity to the database.
     * Uses MinMax Normalisation as default if normalizationType is not specified.
     * @param indicator The indicator to save
     * @return The saved indicator with ID
     * @throws CustomException if validation fails
     */
    @Transactional
    public Indicator save(Indicator indicator) {
        validateIndicator(indicator);
        
        // Set default normalization type if not specified
        if (indicator.getNormalizationType() == null) {
            indicator.setNormalizationType(DEFAULT_NORMALIZATION_TYPE);
            log.info("Setting default normalization type: {}", DEFAULT_NORMALIZATION_TYPE);
        }
        
        log.info("Saving indicator: {}", indicator.getName());
        return indicatorRepository.save(indicator);
    }

    /**
     * Deletes an indicator by ID.
     * @param id The indicator ID to delete
     * @throws CustomException if indicator is not found
     */
    @Transactional
    public void delete(Long id) {
        if (!indicatorRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Indicator not found", HttpStatus.NOT_FOUND);
        }
        log.info("Deleting indicator with ID: {}", id);
        indicatorRepository.deleteById(id);
    }

    /**
     * Sets the category for an indicator.
     * @param indicatorId The indicator ID
     * @param category The category to set
     * @return The updated indicator
     */
    @Transactional
    public Indicator setCategory(Long indicatorId, IndicatorCategory category) {
        Indicator indicator = findById(indicatorId);
        indicator.setCategory(category);
        log.info("Set category for indicator: {}", indicator.getName());
        return indicatorRepository.save(indicator);
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
        log.info("Added score to indicator: {}", indicator.getName());
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
        log.info("Removed score from indicator: {}", indicator.getName());
        return indicatorRepository.save(indicator);
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validates indicator data before saving.
     * @param indicator The indicator to validate
     * @throws CustomException if validation fails
     */
    private void validateIndicator(Indicator indicator) {
        if (indicator == null) {
            throw new CustomException("Indicator data is required", HttpStatus.BAD_REQUEST);
        }
        
        ValidationUtils.validateNotEmpty(indicator.getName(), "Indicator name");

        if (indicator.getWeight() != 0) {
            ValidationUtils.validateIndicatorWeight(indicator.getWeight());
        }
        
        // Check for duplicate names
        Indicator existingIndicator = indicatorRepository.findByName(indicator.getName());
        if (existingIndicator != null && (indicator.getId() == null || !indicator.getId().equals(existingIndicator.getId()))) {
            throw new CustomException("Indicator with this name already exists", HttpStatus.CONFLICT);
        }
    }
}

