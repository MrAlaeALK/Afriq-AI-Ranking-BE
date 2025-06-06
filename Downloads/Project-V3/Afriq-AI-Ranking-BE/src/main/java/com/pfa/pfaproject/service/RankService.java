package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.getFinalScoreAndRankDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Country;
import com.pfa.pfaproject.model.Rank;
import com.pfa.pfaproject.repository.RankRepository;
import com.pfa.pfaproject.util.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
//    private final CountryService countryService;

    /**
     * Returns all ranks in the system.
     * @return List of all ranks
     */
    public List<Rank> findAll() {
        return rankRepository.findAll();
    }

    /**
     * Finds a rank by ID.
     * @param id The rank ID
     * @return The found rank
     * @throws CustomException if rank is not found
     */
    public Rank findById(Long id) {
        return rankRepository.findById(id)
                .orElseThrow(() -> new CustomException("Rank not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds all ranks for a specific year, ordered by final score descending.
     * @param year The year to search
     * @return List of ranks for the specified year
     */
    public List<Rank> findByYearOrderByFinalScoreDesc(int year) {
        return rankRepository.findAllByYearOrderByFinalScoreDesc(year);
    }

    /**
     * Finds a rank by country ID and year.
     * @param countryId The country ID
     * @param year The year
     * @return The found rank
     */
    public Rank findByCountryIdAndYear(Long countryId, int year) {
        return rankRepository.findByCountry_IdAndYear(countryId, year);
    }
    
    /**
     * Checks if a rank exists for a country in a specific year.
     * @param countryId The country ID
     * @param year The year
     * @return true if exists, false otherwise
     */
    public boolean existsByCountryIdAndYear(Long countryId, int year) {
        return rankRepository.findByCountry_IdAndYear(countryId, year) != null;
    }

    /**
     * Saves a rank entity to the database.
     * @param rank The rank to save
     * @return The saved rank with ID
     * @throws CustomException if validation fails
     */
    @Transactional
    public Rank save(Rank rank) {
        rank.setFinalScore(Utils.round(rank.getFinalScore(), 2));
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
        rankRepository.deleteById(id);
    }
    
    /**
     * Updates the rank position (ordinal rank) for all countries in a given year.
     * Rank positions are assigned based on finalScore in descending order.
     * Handles ties by assigning the same rank to countries with identical scores.
     * 
     * @param year The year to update ranks for
     */
    @Transactional
    public void updateRankPositions(int year) {

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
    }


    public List<getFinalScoreAndRankDTO> findAllByYearOrderByRank(int year) {
        return null;
    }

    public void deleteByYear(Integer year) {
    }

//    public List<Rank> orderByFinalScore(List<Rank> ranks){
//        return rankRepository.orderByFinalScoreDesc(ranks);
//    }
}

