package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.GetDimensionsDTO;
import com.pfa.pfaproject.dto.indicator.CreateIndicatorDTO;
import com.pfa.pfaproject.dto.indicator.UpdateIndicatorDTO;
import com.pfa.pfaproject.dto.indicator.IndicatorResponseDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.model.Rank;
import com.pfa.pfaproject.repository.DimensionRepository;
import com.pfa.pfaproject.repository.IndicatorRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashSet;

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
 */
@Service
@Data
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IndicatorService {
    private final IndicatorRepository indicatorRepository;
    private final DimensionRepository dimensionRepository;
    private final DimensionWeightService dimensionWeightService;
    private final RankService rankService;
    
    // Default normalization type constant
    private static final String DEFAULT_NORMALIZATION_TYPE = "MinMax Normalisation";

    public List<Indicator> findAll() {
        return indicatorRepository.findAll();
    }

    public Indicator findById(Long id) {
        return indicatorRepository.findById(id)
                .orElseThrow(() -> new CustomException("Indicator not found", HttpStatus.NOT_FOUND));
    }

    public Indicator findByName(String name) {
        List<Indicator> indicators = indicatorRepository.findByName(name);
        if (indicators.isEmpty()) {
            return null;
        }
        if (indicators.size() > 1) {
            log.warn("Found {} duplicate indicators with name '{}'. Using the first one (ID: {}). Please clean up duplicates.", 
                    indicators.size(), name, indicators.get(0).getId());
        }
        return indicators.get(0);
    }

    public Indicator save(Indicator indicator) {
        
        // Set default normalization type if not specified
        if (indicator.getNormalizationType() == null) {
            indicator.setNormalizationType(DEFAULT_NORMALIZATION_TYPE);
        }

        return indicatorRepository.save(indicator);
    }

    public void delete(Long id) {
        if (!indicatorRepository.existsById(id)) {
            throw new CustomException("Cannot delete: Indicator not found", HttpStatus.NOT_FOUND);
        }
        indicatorRepository.deleteById(id);
    }

    public IndicatorResponseDTO findByIdWithDetails(Long id) {
        Indicator indicator = findById(id);
        return mapToResponseDTO(indicator);
    }

    @Transactional
    public IndicatorResponseDTO createIndicator(CreateIndicatorDTO createDTO) {
        Dimension dimension = dimensionRepository.findById(createDTO.dimensionId())
                .orElseThrow(() -> new CustomException("Dimension non trouvée", HttpStatus.NOT_FOUND));

        // Check if an indicator with the same name already exists for this dimension and year
        Indicator existingIndicator = findByName(createDTO.name());
        if (existingIndicator != null && existingIndicator.getDimension().getId().equals(createDTO.dimensionId())) {
            // Check if this indicator already has a weight for the target year
            boolean hasWeightForYear = existingIndicator.getWeights().stream()
                    .anyMatch(w -> w.getYear().equals(createDTO.year()));
            
            if (hasWeightForYear) {
                throw new CustomException("Un indicateur avec ce nom existe déjà pour cette dimension et cette année.", HttpStatus.BAD_REQUEST);
            } else {
                // Add new weight to existing indicator for new year
                return addWeightToExistingIndicator(existingIndicator, createDTO);
            }
        }

        // Check if rankings exist for this year
        List<Rank> rankingsForYear = rankService.findAllByYear(createDTO.year());
        if (!rankingsForYear.isEmpty()) {
            throw new CustomException(
                String.format("L'ajout de cet indicateur invalidera les classements existants pour %d.", 
                    createDTO.year()),
                HttpStatus.CONFLICT
            );
        }

        // Validate that total weights for this dimension and year won't exceed 100%
        validateWeightLimit(createDTO.dimensionId(), createDTO.year(), createDTO.weight(), null);

        // Create new indicator
        Indicator indicator = Indicator.builder()
                .name(createDTO.name())
                .description(createDTO.description())
                .dimension(dimension)
                .normalizationType(createDTO.normalizationType() != null ? createDTO.normalizationType() : DEFAULT_NORMALIZATION_TYPE)
                .build();

        Indicator savedIndicator = save(indicator);

        // Find the corresponding DimensionWeight for this dimension and year
        DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(createDTO.dimensionId(), createDTO.year());
        if (dimensionWeight == null) {
            throw new CustomException("Aucun poids de dimension trouvé pour l'année " + createDTO.year() + ". Veuillez d'abord créer la dimension pour cette année.", HttpStatus.BAD_REQUEST);
        }

        IndicatorWeight weight = IndicatorWeight.builder()
                .indicator(savedIndicator)
                .year(createDTO.year())
                .weight(createDTO.weight())
                .dimensionWeight(dimensionWeight)
                .build();

        savedIndicator.addWeight(weight);
        savedIndicator = save(savedIndicator);

        return mapToResponseDTO(savedIndicator);
    }

    @Transactional
    public IndicatorResponseDTO forceCreateIndicator(CreateIndicatorDTO createDTO) {
        Dimension dimension = dimensionRepository.findById(createDTO.dimensionId())
                .orElseThrow(() -> new CustomException("Dimension non trouvée", HttpStatus.NOT_FOUND));

        // Check if an indicator with the same name already exists for this dimension and year
        Indicator existingIndicator = findByName(createDTO.name());
        if (existingIndicator != null && existingIndicator.getDimension().getId().equals(createDTO.dimensionId())) {
            // Check if this indicator already has a weight for the target year
            boolean hasWeightForYear = existingIndicator.getWeights().stream()
                    .anyMatch(w -> w.getYear().equals(createDTO.year()));
            
            if (hasWeightForYear) {
                throw new CustomException("Un indicateur avec ce nom existe déjà pour cette dimension et cette année.", HttpStatus.BAD_REQUEST);
            } else {
                // Add new weight to existing indicator for new year (skip ranking validation)
                return forceAddWeightToExistingIndicator(existingIndicator, createDTO);
            }
        }

        // Validate that total weights for this dimension and year won't exceed 100%
        validateWeightLimit(createDTO.dimensionId(), createDTO.year(), createDTO.weight(), null);

        // Create new indicator (skip ranking validation)
        Indicator indicator = Indicator.builder()
                .name(createDTO.name())
                .description(createDTO.description())
                .dimension(dimension)
                .normalizationType(createDTO.normalizationType() != null ? createDTO.normalizationType() : DEFAULT_NORMALIZATION_TYPE)
                .build();

        Indicator savedIndicator = save(indicator);

        // Find the corresponding DimensionWeight for this dimension and year
        DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(createDTO.dimensionId(), createDTO.year());
        if (dimensionWeight == null) {
            throw new CustomException("Aucun poids de dimension trouvé pour l'année " + createDTO.year() + ". Veuillez d'abord créer la dimension pour cette année.", HttpStatus.BAD_REQUEST);
        }

        IndicatorWeight weight = IndicatorWeight.builder()
                .indicator(savedIndicator)
                .year(createDTO.year())
                .weight(createDTO.weight())
                .dimensionWeight(dimensionWeight)
                .build();

        savedIndicator.addWeight(weight);
        savedIndicator = save(savedIndicator);

        log.info("Force created indicator '{}' for year {} - existing rankings may be invalidated", 
                savedIndicator.getName(), createDTO.year());

        return mapToResponseDTO(savedIndicator);
    }

    private IndicatorResponseDTO forceAddWeightToExistingIndicator(Indicator indicator, CreateIndicatorDTO createDTO) {
        // Validate that total weights for this dimension and year won't exceed 100%
        validateWeightLimit(createDTO.dimensionId(), createDTO.year(), createDTO.weight(), null);

        // Find the corresponding DimensionWeight for this dimension and year
        DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(createDTO.dimensionId(), createDTO.year());
        if (dimensionWeight == null) {
            throw new CustomException("Aucun poids de dimension trouvé pour l'année " + createDTO.year() + ". Veuillez d'abord créer la dimension pour cette année.", HttpStatus.BAD_REQUEST);
        }

        IndicatorWeight weight = IndicatorWeight.builder()
                .indicator(indicator)
                .year(createDTO.year())
                .weight(createDTO.weight())
                .dimensionWeight(dimensionWeight)
                .build();

        indicator.addWeight(weight);
        Indicator savedIndicator = save(indicator);

        log.info("Force added weight to existing indicator '{}' for year {} - existing rankings may be invalidated", 
                indicator.getName(), createDTO.year());

        return mapToResponseDTO(savedIndicator);
    }

    private IndicatorResponseDTO addWeightToExistingIndicator(Indicator indicator, CreateIndicatorDTO createDTO) {
        // Check if rankings exist for this year
        List<Rank> rankingsForYear = rankService.findAllByYear(createDTO.year());
        if (!rankingsForYear.isEmpty()) {
            throw new CustomException(
                String.format("L'ajout de cet indicateur invalidera les classements existants pour %d.", 
                    createDTO.year()),
                HttpStatus.CONFLICT
            );
        }

        // Validate that total weights for this dimension and year won't exceed 100%
        validateWeightLimit(createDTO.dimensionId(), createDTO.year(), createDTO.weight(), null);

        // Find the corresponding DimensionWeight for this dimension and year
        DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(createDTO.dimensionId(), createDTO.year());
        if (dimensionWeight == null) {
            throw new CustomException("Aucun poids de dimension trouvé pour l'année " + createDTO.year() + ". Veuillez d'abord créer la dimension pour cette année.", HttpStatus.BAD_REQUEST);
        }

        IndicatorWeight weight = IndicatorWeight.builder()
                .indicator(indicator)
                .year(createDTO.year())
                .weight(createDTO.weight())
                .dimensionWeight(dimensionWeight)
                .build();

        indicator.addWeight(weight);
        Indicator savedIndicator = save(indicator);

        return mapToResponseDTO(savedIndicator);
    }

    @Transactional
    public IndicatorResponseDTO updateIndicator(Long id, UpdateIndicatorDTO updateDTO) {
        Indicator indicator = findById(id);

        // Check if new name conflicts with existing indicator for the same dimension and year (excluding current one)
        Indicator existingIndicator = findByName(updateDTO.name());
        if (existingIndicator != null && !existingIndicator.getId().equals(id)) {
            // Check if it's for the same dimension and year
            if (existingIndicator.getDimension().getId().equals(updateDTO.dimensionId())) {
                boolean hasWeightForYear = existingIndicator.getWeights().stream()
                        .anyMatch(w -> w.getYear().equals(updateDTO.year()));
                if (hasWeightForYear) {
                    throw new CustomException("Un indicateur avec ce nom existe déjà pour cette dimension et cette année.", HttpStatus.BAD_REQUEST);
                }
            }
        }

        // Validate that total weights for this dimension and year won't exceed 100%
        validateWeightLimit(updateDTO.dimensionId(), updateDTO.year(), updateDTO.weight(), id);

        indicator.setName(updateDTO.name());
        indicator.setDescription(updateDTO.description());
        
        if (updateDTO.normalizationType() != null) {
            indicator.setNormalizationType(updateDTO.normalizationType());
        }

        Dimension dimension = dimensionRepository.findById(updateDTO.dimensionId())
                .orElseThrow(() -> new CustomException("Dimension non trouvée", HttpStatus.NOT_FOUND));
        indicator.setDimension(dimension);

        IndicatorWeight existingWeight = indicator.getWeights().stream()
                .filter(w -> w.getYear().equals(updateDTO.year()))
                .findFirst()
                .orElse(null);
        
        if (existingWeight != null) {
            existingWeight.setWeight(updateDTO.weight());
        } else {
            // Find the corresponding DimensionWeight for this dimension and year
            DimensionWeight dimensionWeight = dimensionWeightService.findByCategoryAndYear(updateDTO.dimensionId(), updateDTO.year());
            if (dimensionWeight == null) {
                throw new CustomException("Aucun poids de dimension trouvé pour l'année " + updateDTO.year() + ". Veuillez d'abord créer la dimension pour cette année.", HttpStatus.BAD_REQUEST);
            }
            
            IndicatorWeight newWeight = IndicatorWeight.builder()
                    .indicator(indicator)
                    .year(updateDTO.year())
                    .weight(updateDTO.weight())
                    .dimensionWeight(dimensionWeight)  // Link to DimensionWeight
                    .build();
            indicator.addWeight(newWeight);
        }
        
        Indicator savedIndicator = save(indicator);
        
        // Disabled auto-normalization to preserve user-set weights  
        // normalizeWeightsForDimensionYear(updateDTO.dimensionId(), updateDTO.year());
        
        return mapToResponseDTO(savedIndicator);
    }

    @Transactional
    public void deleteIndicator(Long id) {
        // Get indicator details before deletion for validation
        Indicator indicator = findById(id);
        
        // Check if rankings exist for any years this indicator has weights for
        List<Integer> affectedYears = indicator.getWeights().stream()
                .map(IndicatorWeight::getYear)
                .distinct()
                .toList();
        
        List<Integer> yearsWithRankings = new ArrayList<>();
        for (Integer year : affectedYears) {
            List<Rank> rankingsForYear = rankService.findAllByYear(year);
            if (!rankingsForYear.isEmpty()) {
                yearsWithRankings.add(year);
            }
        }
        
        if (!yearsWithRankings.isEmpty()) {
            String yearsList = yearsWithRankings.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
                    
            throw new CustomException(
                String.format("Cet indicateur est utilisé dans les classements %s.", yearsList),
                HttpStatus.CONFLICT
            );
        }
        
        Long dimensionId = indicator.getDimension().getId();
        
        // Get all years that have weights for this indicator (for potential auto-normalization)
        List<Integer> yearsToNormalize = indicator.getWeights().stream()
                .map(IndicatorWeight::getYear)
                .distinct()
                .toList();
        
        // Delete the indicator
        delete(id);
        
        log.info("Successfully deleted indicator '{}' (ID: {})", indicator.getName(), id);
        
        // Note: Auto-normalization is disabled to preserve user-set weights
        // Users should manually adjust weights if needed
    }

    /**
     * Force delete an indicator by ID (bypasses ranking validation)
     */
    @Transactional
    public void forceDeleteIndicator(Long id) {
        Indicator indicator = findById(id);
        Long dimensionId = indicator.getDimension().getId();
        
        // Delete the indicator
        delete(id);
        
        log.info("Force deleted indicator '{}' (ID: {}) - rankings may need regeneration", indicator.getName(), id);
    }

    public List<Indicator> findByDimensionId(Long dimensionId) {
        return indicatorRepository.findByDimensionId(dimensionId);
    }

    public Integer getWeightByYear(Long indicatorId, Integer year) {
        Indicator indicator = findById(indicatorId);
        return indicator.getWeightForYear(year);
    }

    @Transactional
    public void bulkDeleteIndicators(List<Long> indicatorIds) {
        // Get affected dimensions and years before deletion for auto-normalization
        List<Indicator> indicatorsToDelete = indicatorRepository.findAllById(indicatorIds);
        
        // Collect unique dimension/year combinations that will be affected
        Set<String> dimensionYearCombinations = indicatorsToDelete.stream()
                .flatMap(indicator -> indicator.getWeights().stream()
                        .map(weight -> indicator.getDimension().getId() + ":" + weight.getYear()))
                .collect(Collectors.toSet());
        
        // Delete the indicators
        indicatorRepository.deleteAllById(indicatorIds);
        
        // Disabled auto-normalization to preserve user-set weights
        // Auto-normalize weights for each affected dimension/year combination
        // for (String combination : dimensionYearCombinations) {
        //     String[] parts = combination.split(":");
        //     Long dimensionId = Long.parseLong(parts[0]);
        //     Integer year = Integer.parseInt(parts[1]);
        //     
        //     normalizeWeightsForDimensionYear(dimensionId, year);
        //     log.info("Auto-normalized weights for dimension {} year {} after bulk deleting indicators", 
        //             dimensionId, year);
        // }
    }

    /**
     * Normalizes weights for a dimension and year to sum to 100%
     * Uses a proper rounding algorithm to ensure total is exactly 100%
     */
    @Transactional
    public void normalizeWeightsForDimensionYear(Long dimensionId, Integer year) {
        List<Indicator> indicators = indicatorRepository.findByDimensionId(dimensionId);
        
        // Get all indicator weights for this dimension and year
        List<IndicatorWeight> weightsToNormalize = indicators.stream()
                .flatMap(indicator -> indicator.getWeights().stream())
                .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                .filter(weight -> weight.getWeight() != null)
                .collect(Collectors.toList());
        
        if (weightsToNormalize.isEmpty()) {
            log.warn("No weights found for dimension {} year {} - nothing to normalize", dimensionId, year);
            return;
        }
        
        // Calculate current total weight
        int totalWeight = weightsToNormalize.stream()
                .mapToInt(weight -> weight.getWeight())
                .sum();
        
        if (totalWeight <= 0) {
            log.warn("Total weight is 0 or negative for dimension {} year {} - cannot normalize", dimensionId, year);
            return;
        }
        
        // Calculate exact normalized weights (as doubles)
        List<Double> exactWeights = weightsToNormalize.stream()
                .map(weight -> (weight.getWeight() * 100.0) / totalWeight)
                .collect(Collectors.toList());
        
        // Apply proper rounding algorithm to ensure total is exactly 100
        List<Integer> normalizedWeights = properRoundToSum(exactWeights, 100);
        
        // Update the weights
        for (int i = 0; i < weightsToNormalize.size(); i++) {
            weightsToNormalize.get(i).setWeight(normalizedWeights.get(i));
        }
        
        // Save the indicators
        Set<Indicator> indicatorsToSave = weightsToNormalize.stream()
                .map(IndicatorWeight::getIndicator)
                .collect(Collectors.toSet());
        indicatorRepository.saveAll(indicatorsToSave);
        
        // Verify the total
        int finalTotal = normalizedWeights.stream().mapToInt(Integer::intValue).sum();
        log.info("Normalized weights for dimension {} year {} (total was {}% -> {}%)", 
                dimensionId, year, totalWeight, finalTotal);
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
     * Normalizes weights for all dimensions and years
     */
    @Transactional
    public void normalizeAllWeights() {
        // Get all dimensions
        List<Dimension> dimensions = dimensionRepository.findAll();
        
        for (Dimension dimension : dimensions) {
            // Get all years for this dimension
            Set<Integer> years = indicatorRepository.findByDimensionId(dimension.getId()).stream()
                    .flatMap(indicator -> indicator.getWeights().stream())
                    .map(IndicatorWeight::getYear)
                    .collect(Collectors.toSet());
            
            // Normalize each year
            for (Integer year : years) {
                normalizeWeightsForDimensionYear(dimension.getId(), year);
            }
        }
    }

     // Validates that adding/updating an indicator weight won't exceed 100% total for the dimension and year
    private void validateWeightLimit(Long dimensionId, Integer year, Integer newWeight, Long excludeIndicatorId) {
        try {
            // Get all indicators for this dimension
            List<Indicator> dimensionIndicators = indicatorRepository.findByDimensionId(dimensionId).stream()
                    .filter(indicator -> excludeIndicatorId == null || !indicator.getId().equals(excludeIndicatorId)) // Exclude current indicator if updating
                    .collect(Collectors.toList());
            
            // Calculate current total weight for this year (stored as integers 1-100)
            int currentTotalWeight = dimensionIndicators.stream()
                    .mapToInt(indicator -> {
                        if (indicator.getWeights() == null) return 0;
                        return indicator.getWeights().stream()
                                .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                                .mapToInt(weight -> weight.getWeight() != null ? weight.getWeight() : 0)
                                .sum();
                    })
                    .sum();
            
            // Add new weight to current total
            int newWeightValue = newWeight != null ? newWeight : 0;
            int proposedTotalWeight = currentTotalWeight + newWeightValue;
        
            // Throw an error if total would exceed 100%
            if (proposedTotalWeight > 100) {
                throw new CustomException(
                    String.format("Le poids total ne peut pas dépasser 100%%. " +
                                "Poids actuel de la dimension: %d%%, " +
                                "Poids maximum autorisé: %d%%", 
                                currentTotalWeight, 
                                (100 - currentTotalWeight)), 
                    HttpStatus.BAD_REQUEST
                );
            }
        } catch (CustomException e) {
            throw e; // Re-throw our custom validation error
        } catch (Exception e) {
            log.error("Error in weight validation: ", e);
            // If validation fails due to unexpected error, allow the operation to continue
            log.warn("Weight validation failed with error, allowing operation to continue");
        }
    }

    /**
     * Gets the current weight total for a dimension and year
     */
    public int getDimensionWeightTotal(Long dimensionId, Integer year) {
        List<Indicator> dimensionIndicators = indicatorRepository.findByDimensionId(dimensionId);
        
        return dimensionIndicators.stream()
                .mapToInt(indicator -> {
                    if (indicator.getWeights() == null) return 0;
                    return indicator.getWeights().stream()
                            .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                            .mapToInt(weight -> weight.getWeight() != null ? weight.getWeight() : 0)
                            .sum();
                })
                .sum();
    }

    /**
     * Adjusts effective weight when total for dimension/year exceeds 100%
     * Simple approach: subtract excess from the largest effective weight
     */
    private Integer adjustEffectiveWeightForDimensionYear(Long currentIndicatorId, Integer year, 
                                                         Integer currentEffectiveWeight, Integer dimensionWeight) {
        try {
            // Get all indicators for this dimension
            List<Indicator> dimensionIndicators = indicatorRepository.findByDimensionId(
                    indicatorRepository.findById(currentIndicatorId).orElseThrow().getDimension().getId());
            
            // Calculate all effective weights for this dimension/year
            List<EffectiveWeightInfo> allEffectiveWeights = new ArrayList<>();
            int totalEffectiveWeight = 0;
            
            for (Indicator indicator : dimensionIndicators) {
                IndicatorWeight indicatorWeight = indicator.getWeights().stream()
                        .filter(w -> w.getYear().equals(year))
                        .findFirst()
                        .orElse(null);
                
                if (indicatorWeight != null && indicatorWeight.getWeight() != null) {
                    // Calculate effective weight
                    double exactEffectiveWeight = (dimensionWeight.doubleValue() * indicatorWeight.getWeight().doubleValue()) / 100.0;
                    int roundedEffectiveWeight = (int) Math.round(exactEffectiveWeight);
                    
                    // Use the current weight for the current indicator, calculated weight for others
                    if (indicator.getId().equals(currentIndicatorId)) {
                        roundedEffectiveWeight = currentEffectiveWeight;
                    }
                    
                    allEffectiveWeights.add(new EffectiveWeightInfo(indicator.getId(), roundedEffectiveWeight));
                    totalEffectiveWeight += roundedEffectiveWeight;
                }
            }
            
            // If total exceeds dimension weight, adjust the largest weight
            if (totalEffectiveWeight > dimensionWeight) {
                int excess = totalEffectiveWeight - dimensionWeight;
                
                // Find the indicator with the largest effective weight
                EffectiveWeightInfo largestWeight = allEffectiveWeights.stream()
                        .max((w1, w2) -> Integer.compare(w1.effectiveWeight, w2.effectiveWeight))
                        .orElse(null);
                
                if (largestWeight != null) {
                    // If current indicator has the largest weight, adjust it
                    if (largestWeight.indicatorId.equals(currentIndicatorId)) {
                        int adjustedWeight = currentEffectiveWeight - excess;
                        log.info("Adjusted effective weight for indicator {} from {}% to {}% (excess: {}%) for year {}", 
                                currentIndicatorId, currentEffectiveWeight, adjustedWeight, excess, year);
                        return Math.max(0, adjustedWeight); // Ensure non-negative
                    }
                }
            }
            
            return currentEffectiveWeight;
            
        } catch (Exception e) {
            log.warn("Failed to adjust effective weight for indicator {} year {}: {}", 
                    currentIndicatorId, year, e.getMessage());
            return currentEffectiveWeight; // Return original weight if adjustment fails
        }
    }
    
    /**
     * Helper class for effective weight adjustment calculations
     */
    private static class EffectiveWeightInfo {
        Long indicatorId;
        int effectiveWeight;
        
        EffectiveWeightInfo(Long indicatorId, int effectiveWeight) {
            this.indicatorId = indicatorId;
            this.effectiveWeight = effectiveWeight;
        }
    }

    public IndicatorResponseDTO mapToResponseDTO(Indicator indicator) {
        GetDimensionsDTO dimensionDTO = null;
        if (indicator.getDimension() != null) {
            dimensionDTO = new GetDimensionsDTO(
                    indicator.getDimension().getId(),
                    indicator.getDimension().getName(),
                    indicator.getDimension().getDescription(),
                    null // No specific year for dimension in this context
            );
        }

        // Create weightsByYear map from indicator weights (keep as integers)
        Map<Integer, Integer> weightsByYear = indicator.getWeights().stream()
                .filter(weight -> weight.getYear() != null)  // Filter out null years
                .collect(Collectors.toMap(
                    IndicatorWeight::getYear,
                    weight -> weight.getWeight() != null ? weight.getWeight() : 0,
                    (existing, replacement) -> {
                        // Handle duplicate years by keeping the first weight found
                        log.warn("Duplicate year found for indicator '{}' with weights {} and {}. Keeping first weight.", 
                                indicator.getName(), existing, replacement);
                        return existing;
                    }
                ));

        // Calculate effective weights using simple adjustment for 101% issue
        Map<Integer, Integer> effectiveWeightsByYear = new HashMap<>();
        Map<Integer, Integer> dimensionWeightsByYear = new HashMap<>();
        Set<Integer> processedYears = new HashSet<>(); // Track processed years to handle duplicates
        
        for (IndicatorWeight indicatorWeight : indicator.getWeights()) {
            Integer year = indicatorWeight.getYear();
            
            // Skip processing if year is null or already processed
            if (year == null || processedYears.contains(year)) {
                if (year != null && processedYears.contains(year)) {
                    log.debug("Skipping duplicate year {} for indicator '{}'", year, indicator.getName());
                }
                continue;
            }
            
            processedYears.add(year); // Mark this year as processed
            
            Integer indicatorWeightValue = indicatorWeight.getWeight();
            
            // Get dimension weight for this year
            var dimensionWeight = dimensionWeightService.findByCategoryAndYear(
                    indicator.getDimension().getId(), year);
            
            if (dimensionWeight != null && dimensionWeight.getDimensionWeight() != null) {
                Integer dimensionWeightValue = dimensionWeight.getDimensionWeight();
                dimensionWeightsByYear.put(year, dimensionWeightValue);
                
                // Calculate effective weight: (dimension weight * indicator weight) / 100
                if (indicatorWeightValue != null) {
                    double exactEffectiveWeight = (dimensionWeightValue.doubleValue() * indicatorWeightValue.doubleValue()) / 100.0;
                    int roundedEffectiveWeight = (int) Math.round(exactEffectiveWeight);
                    
                    // Apply simple adjustment for 101% issue if needed
                    roundedEffectiveWeight = adjustEffectiveWeightForDimensionYear(
                            indicator.getId(), year, roundedEffectiveWeight, dimensionWeightValue);
                    
                    effectiveWeightsByYear.put(year, roundedEffectiveWeight);
                }
            }
        }

        return new IndicatorResponseDTO(
                indicator.getId(),
                indicator.getName(),
                indicator.getDescription(),
                indicator.getNormalizationType(),
                indicator.getAvailableYears(),
                dimensionDTO,
                weightsByYear,
                effectiveWeightsByYear,
                dimensionWeightsByYear,
                indicator.getCreatedDate(),
                indicator.getLastModifiedDate()
        );
    }
}