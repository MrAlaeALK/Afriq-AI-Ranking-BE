package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Rank;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.CountryRepository;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Country entities and their relationships.
 * ===========================================================
 * 
 * This service handles all operations related to Country entities including:
 * - CRUD operations for country management
 * - Relationship management with scores and ranks
 * - Country data validation
 * - Regional data queries
 * 
 * Countries are a core part of the ranking system, representing the African nations
 * being evaluated in the Afriq-AI Ranking system.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CountryService {
    private final CountryRepository countryRepository;

    // ========== QUERY METHODS ==========

    /**
     * Returns all countries in the system.
     * @return List of all countries
     */
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    /**
     * Finds a country by ID.
     * @param id The country ID
     * @return The found country
     * @throws CustomException if country is not found
     */
    public Country findById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Country not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds a country by name.
     * @param name The country name to search
     * @return The found country
     * @throws CustomException if country is not found
     */
    public Country findByName(String name) {
        return countryRepository.findByName(name);
    }

    /**
     * Finds a country by code.
     * @param code The country code to search (2-3 letter code)
     * @return The found country
     * @throws CustomException if country is not found
     */
    public Country findByCode(String code) {
        return countryRepository.findByCode(code);
    }

    /**
     * Finds all countries in a specific region.
     * @param region The region name to search
     * @return List of countries in the region
     */
    public List<Country> findByRegion(String region) {
        return countryRepository.findByRegion(region);
    }

    /**
     * Saves a country entity to the database.
     * @param country The country to save
     * @return The saved country with ID
     * @throws CustomException if validation fails
     */
    @Transactional
    public Country save(Country country) {
        return countryRepository.save(country);
    }

    /**
     * Deletes a country by ID.
     * @param id The country ID to delete
     * @throws CustomException if country is not found
     */
    @Transactional
    public void delete(Long id) {
        if (!countryRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Country not found", HttpStatus.NOT_FOUND);
        }
        countryRepository.deleteById(id);
    }

    /**
     * Adds a rank to a country.
     * @param countryId The country ID
     * @param rank The rank to add
     * @return The updated country
     */
    @Transactional
    public Country addRank(Long countryId, Rank rank) {
        Country country = findById(countryId);
        country.addRank(rank);
        log.info("Added rank to country: {}", country.getName());
        return countryRepository.save(country);
    }

    /**
     * Adds a score to a country.
     * @param countryId The country ID
     * @param score The score to add
     * @return The updated country
     */
    @Transactional
    public Country addScore(Long countryId, Score score) {
        Country country = findById(countryId);
        country.addScore(score);
        log.info("Added score to country: {}", country.getName());
        return countryRepository.save(country);
    }
}

