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
import java.util.stream.IntStream;

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
     * Uses a proper rounding algorithm to ensure total is exactly 100%
     */
    @Transactional
    public void normalizeDimensionWeightsForYear(Integer year) {
        List<DimensionWeight> dimensionWeights = dimensionWeightRepository.findByYear(year);
        
        if (dimensionWeights.isEmpty()) {
            log.warn("No dimension weights found for year {} - nothing to normalize", year);
            return;
        }
        
        // Filter out null weights
        List<DimensionWeight> validWeights = dimensionWeights.stream()
                .filter(dw -> dw.getDimensionWeight() != null)
                .collect(Collectors.toList());
        
        if (validWeights.isEmpty()) {
            log.warn("No valid dimension weights found for year {} - nothing to normalize", year);
            return;
        }
        
        // Calculate current total weight (stored as integers 1-100)
        int totalWeight = validWeights.stream()
                .mapToInt(DimensionWeight::getDimensionWeight)
                .sum();
        
        if (totalWeight <= 0) {
            log.warn("Total dimension weight is 0 or negative for year {} - cannot normalize", year);
            return;
        }
        
        // Calculate exact normalized weights (as doubles)
        List<Double> exactWeights = validWeights.stream()
                .map(dw -> (dw.getDimensionWeight() * 100.0) / totalWeight)
                .collect(Collectors.toList());
        
        // Apply proper rounding algorithm to ensure total is exactly 100
        List<Integer> normalizedWeights = properRoundToSum(exactWeights, 100);
        
        // Update the weights
        for (int i = 0; i < validWeights.size(); i++) {
            validWeights.get(i).setDimensionWeight(normalizedWeights.get(i));
        }
        
        dimensionWeightRepository.saveAll(validWeights);
        
        // Verify the total
        int finalTotal = normalizedWeights.stream().mapToInt(Integer::intValue).sum();
        log.info("Normalized dimension weights for year {} (total was {}% -> {}%)", 
                year, totalWeight, finalTotal);
    }
    
    /**
     * Proper rounding algorithm that ensures the sum of rounded values equals the target sum.
     * Uses the largest remainder method to distribute rounding errors.
     */
    private List<Integer> properRoundToSum(List<Double> values, int targetSum) {
        // Step 1: Get the floor of each value
        List<Integer> floorValues = values.stream()
                .map(v -> (int) Math.floor(v))
                .collect(Collectors.toList());
        
        // Step 2: Calculate remainders and current sum
        List<Double> remainders = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            remainders.add(values.get(i) - floorValues.get(i));
        }
        
        int currentSum = floorValues.stream().mapToInt(Integer::intValue).sum();
        int deficit = targetSum - currentSum;
        
        // Step 3: Distribute the deficit by adding 1 to values with largest remainders
        // Create indexed list for sorting
        List<Integer> indices = IntStream.range(0, remainders.size()).boxed().collect(Collectors.toList());
        
        // Sort indices by remainder in descending order
        indices.sort((i, j) -> Double.compare(remainders.get(j), remainders.get(i)));
        
        // Add 1 to the 'deficit' number of values with largest remainders
        List<Integer> result = new ArrayList<>(floorValues);
        for (int i = 0; i < Math.min(deficit, indices.size()); i++) {
            int index = indices.get(i);
            result.set(index, result.get(index) + 1);
        }
        
        return result;
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
