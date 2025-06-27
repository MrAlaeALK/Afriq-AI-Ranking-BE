package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.GetYearDimensionsDTO;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.repository.DimensionWeightRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DimensionWeightService {
    private final DimensionWeightRepository dimensionWeightRepository;

    public List<DimensionWeight> getAllByYear(Integer  year) {
        return dimensionWeightRepository.findByYear(year);
    }

    public DimensionWeight save(DimensionWeight dimensionWeight) {
        return dimensionWeightRepository.save(dimensionWeight);
    }

    public DimensionWeight findByCategoryAndYear(Long dimensionId, Integer  year) {
        return dimensionWeightRepository.findByDimension_IdAndYear(dimensionId, year);
    }

    public List<DimensionWeight> findByYear(Integer  year) {
        return dimensionWeightRepository.findByYear(year);
    }

    public List<GetYearDimensionsDTO> getYearDimensions(Integer  year) {
        List<DimensionWeight> dimensionWeights = dimensionWeightRepository.findByYear(year);
        List<GetYearDimensionsDTO> yearDimensions = new ArrayList<>();
        for (DimensionWeight dimensionWeight : dimensionWeights) {
            yearDimensions.add(new GetYearDimensionsDTO(dimensionWeight.getDimension().getId(), dimensionWeight.getDimension().getName(), dimensionWeight.getDimension().getDescription(), dimensionWeight.getDimensionWeight()));
        }
        return yearDimensions;
    }

    /**
     * Normalizes dimension weights for a specific year to sum to 100%
     */
    @Transactional
    public void normalizeDimensionWeightsForYear(Integer year) {
        List<DimensionWeight> dimensionWeights = dimensionWeightRepository.findByYear(year);
        
        if (dimensionWeights.isEmpty()) {
            log.warn("No dimension weights found for year {} - nothing to normalize", year);
            return;
        }
        
        // Calculate current total weight
        double totalWeight = dimensionWeights.stream()
                .mapToDouble(dw -> dw.getDimensionWeight() != null ? dw.getDimensionWeight() : 0.0)
                .sum();
        
        // Normalize proportionally to sum to 1.0 (100%)
        if (totalWeight > 0.0) {
            double scaleFactor = 1.0 / totalWeight;
            
            dimensionWeights.forEach(dimensionWeight -> {
                if (dimensionWeight.getDimensionWeight() != null) {
                    double normalizedWeight = dimensionWeight.getDimensionWeight() * scaleFactor;
                    dimensionWeight.setDimensionWeight(normalizedWeight);
                }
            });
            
            dimensionWeightRepository.saveAll(dimensionWeights);
            log.info("Normalized dimension weights for year {} by factor {} (total was {}% -> 100%)", 
                    year, scaleFactor, totalWeight * 100);
        }
    }

    /**
     * Normalizes dimension weights for all years
     */
    @Transactional
    public void normalizeAllDimensionWeights() {
        // Get all years that have dimension weights
        Set<Integer> years = dimensionWeightRepository.findAll().stream()
                .map(DimensionWeight::getYear)
                .collect(Collectors.toSet());
        
        // Normalize each year
        for (Integer year : years) {
            normalizeDimensionWeightsForYear(year);
        }
    }

    /**
     * Creates or updates a dimension weight and auto-normalizes for the year
     */
    @Transactional
    public DimensionWeight saveAndNormalize(DimensionWeight dimensionWeight) {
        DimensionWeight saved = dimensionWeightRepository.save(dimensionWeight);
        
        // Auto-normalize for this year
        normalizeDimensionWeightsForYear(dimensionWeight.getYear());
        
        return saved;
    }
}
