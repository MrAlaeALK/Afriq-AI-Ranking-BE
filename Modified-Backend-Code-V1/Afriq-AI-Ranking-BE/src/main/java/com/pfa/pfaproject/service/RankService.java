package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Rank;
import com.pfa.pfaproject.repository.RankRepository;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing Rank entities and computing country rankings.
 * ===========================================================
 * 
 * This service handles all operations related to Rank entities including:
 * - CRUD operations for rank management
 * - Position calculation and rank ordering
 * - Historical ranking data retrieval
 * - Ties and rank position handling
 * 
 * Ranks represent a country's position in the Afriq-AI Ranking system for a specific year,
 * calculated based on the weighted scores across all indicators.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankService {
    private final RankRepository rankRepository;
    private final CountryService countryService;

    // ========== QUERY METHODS ==========

    /**
     * Returns all ranks in the system.
     * @return List of all ranks
     */
    public List<Rank> findAll() {
        log.info("Retrieving all ranks");
        return rankRepository.findAll();
    }

    /**
     * Finds a rank by ID.
     * @param id The rank ID
     * @return The found rank
     * @throws CustomException if rank is not found
     */
    public Rank findById(Long id) {
        log.info("Finding rank with ID: {}", id);
        return rankRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rank not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds all ranks for a specific year, ordered by final score descending.
     * @param year The year to search
     * @return List of ranks for the specified year
     */
    public List<Rank> findByYearOrderByFinalScoreDesc(int year) {
        ValidationUtils.validateYear(year);
        log.info("Finding ranks for year: {} ordered by score", year);
        return rankRepository.findAllByYearOrderByFinalScoreDesc(year);
    }

    /**
     * Finds a rank by country ID and year.
     * @param countryId The country ID
     * @param year The year
     * @return The found rank
     * @throws CustomException if rank is not found
     */
    public Rank findByCountryIdAndYear(Long countryId, int year) {
        ValidationUtils.validateYear(year);
        log.info("Finding rank for country ID: {} and year: {}", countryId, year);
        Rank rank = rankRepository.findByCountry_IdAndYear(countryId, year);
        if (rank == null) {
            throw new CustomException(
                    String.format("Rank not found for country ID %d in year %d", countryId, year),
                    HttpStatus.NOT_FOUND);
        }
        return rank;
    }
    
    /**
     * Checks if a rank exists for a country in a specific year.
     * @param countryId The country ID
     * @param year The year
     * @return true if exists, false otherwise
     */
    public boolean existsByCountryIdAndYear(Long countryId, int year) {
        ValidationUtils.validateYear(year);
        return rankRepository.findByCountry_IdAndYear(countryId, year) != null;
    }

    // ========== COMMAND METHODS ==========

    /**
     * Saves a rank entity to the database.
     * @param rank The rank to save
     * @return The saved rank with ID
     * @throws CustomException if validation fails
     */
    @Transactional
    public Rank save(Rank rank) {
        validateRank(rank);
        log.info("Saving rank for country: {}, year: {}", 
                rank.getCountry() != null ? rank.getCountry().getName() : "unknown", 
                rank.getYear());
        return rankRepository.save(rank);
    }

    /**
     * Deletes a rank by ID.
     * @param id The rank ID to delete
     * @throws CustomException if rank is not found
     */
    @Transactional
    public void delete(Long id) {
        if (!rankRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Rank not found", HttpStatus.NOT_FOUND);
        }
        log.info("Deleting rank with ID: {}", id);
        rankRepository.deleteById(id);
    }
    
    /**
     * Creates a new rank for a country in a specific year.
     * @param countryId The country ID
     * @param year The year
     * @param finalScore The final calculated score
     * @return The created rank
     * @throws CustomException if rank already exists
     */
    @Transactional
    public Rank createRank(Long countryId, int year, double finalScore) {
        ValidationUtils.validateYear(year);
        Country country = countryService.findById(countryId);
        
        // Check if rank already exists
        if (rankRepository.findByCountry_IdAndYear(countryId, year) != null) {
            throw new CustomException(
                    String.format("Rank for country %s in year %d already exists", 
                            country.getName(), year),
                    HttpStatus.CONFLICT);
        }
        
        Rank rank = new Rank();
        rank.setCountry(country);
        rank.setYear(year);
        rank.setFinalScore(finalScore);
        // Initially set rank to 0, will be updated by updateRankPositions
        rank.setRank(0);
        
        Rank savedRank = rankRepository.save(rank);
        log.info("Created rank for country: {}, year: {}", country.getName(), year);
        
        // Update all rank positions for this year
        updateRankPositions(year);
        
        return savedRank;
    }
    
    /**
     * Updates the final score for a country in a specific year and recalculates rankings.
     * @param countryId The country ID
     * @param year The year
     * @param finalScore The new final score
     * @return The updated rank
     * @throws CustomException if rank doesn't exist
     */
    @Transactional
    public Rank updateFinalScore(Long countryId, int year, double finalScore) {
        ValidationUtils.validateYear(year);
        Rank rank = findByCountryIdAndYear(countryId, year);
        
        rank.setFinalScore(finalScore);
        rankRepository.save(rank);
        log.info("Updated final score for country ID: {}, year: {}", countryId, year);
        
        // Recalculate all rankings for this year
        updateRankPositions(year);
        
        return rank;
    }
    
    // ========== COMPUTATION METHODS ==========
    
    /**
     * Updates the rank position (ordinal rank) for all countries in a given year.
     * Rank positions are assigned based on finalScore in descending order.
     * Handles ties by assigning the same rank to countries with identical scores.
     * 
     * @param year The year to update ranks for
     */
    @Transactional
    public void updateRankPositions(int year) {
        ValidationUtils.validateYear(year);
        log.info("Updating rank positions for year: {}", year);
        List<Rank> ranks = rankRepository.findAllByYearOrderByFinalScoreDesc(year);
        
        int position = 1;
        double lastScore = -1;
        int lastPosition = 0;
        
        for (Rank rank : ranks) {
            // If score is the same as the previous country, assign the same rank (tie)
            if (position > 1 && rank.getFinalScore() == lastScore) {
                rank.setRank(lastPosition);
            } else {
                rank.setRank(position);
                lastPosition = position;
            }
            
            lastScore = rank.getFinalScore();
            position++;
            
            rankRepository.save(rank);
        }
        
        log.info("Updated rank positions for {} countries in year {}", ranks.size(), year);
    }

    // ========== VALIDATION METHODS ==========

    /**
     * Validates rank data before saving.
     * @param rank The rank to validate
     * @throws CustomException if validation fails
     */
    private void validateRank(Rank rank) {
        if (rank == null) {
            throw new CustomException("Rank data is required", HttpStatus.BAD_REQUEST);
        }
        
        if (rank.getCountry() == null) {
            throw new CustomException("Country is required for rank", HttpStatus.BAD_REQUEST);
        }
        
        ValidationUtils.validateYear(rank.getYear());
        
        // Validate finalScore is not negative
        if (rank.getFinalScore() < 0) {
            throw new CustomException("Final score cannot be negative", HttpStatus.BAD_REQUEST);
        }
        
        // Check for duplicate country-year combination
        Rank existingRank = rankRepository.findByCountry_IdAndYear(
                rank.getCountry().getId(), rank.getYear());
        
        if (existingRank != null && (rank.getId() == null || !rank.getId().equals(existingRank.getId()))) {
            throw new CustomException(
                    String.format("Rank for country %s in year %d already exists", 
                            rank.getCountry().getName(), rank.getYear()),
                    HttpStatus.CONFLICT);
        }
    }
}

