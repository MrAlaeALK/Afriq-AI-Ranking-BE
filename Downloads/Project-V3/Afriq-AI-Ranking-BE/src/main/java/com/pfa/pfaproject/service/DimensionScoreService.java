package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Score.getScoresByYearDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.DimensionScore;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.DimensionScoreRepository;
import com.pfa.pfaproject.util.Utils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DimensionScoreService {
    private final DimensionScoreRepository dimensionScoreRepository;

    public DimensionScore save(DimensionScore dimensionScore) {
        dimensionScore.setScore(Utils.round(dimensionScore.getScore(), 2));
        return dimensionScoreRepository.save(dimensionScore);
    }

    public List<DimensionScore> findAll() {
        return dimensionScoreRepository.findAll();
    }

//    public List<DimensionScore> findByYear(int year) {
//        return dimensionScoreRepository.findByYear(year);
//    }

    public DimensionScore findByCountryIdAndDimensionIdAndYear(Long countryId, Long dimensionId, int year) {
        return dimensionScoreRepository.findByCountry_IdAndDimension_IdAndYear(countryId, dimensionId, year);
    }

    public void deleteByYear(Integer year) {

        try {
            // Delete all dimension scores for the year
            dimensionScoreRepository.deleteByYear(year);

        } catch (Exception e) {
            throw new CustomException("Failed to delete dimension scores for year " + year + ": " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Finds all dimension scores for a specific year
     * @param year The year to search for
     * @return List of dimension scores for the specified year
     */
    public List<DimensionScore> findByYear(Integer year) {

        try {
            List<DimensionScore> dimensionScores = dimensionScoreRepository.findByYear(year);

            return dimensionScores;

        } catch (Exception e) {
            throw new CustomException("Failed to retrieve dimension scores for year " + year + ": " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
