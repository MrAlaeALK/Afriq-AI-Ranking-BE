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
import com.pfa.pfaproject.repository.DimensionRepository;
import com.pfa.pfaproject.repository.IndicatorRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return indicatorRepository.findByName(name);
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

    private IndicatorResponseDTO addWeightToExistingIndicator(Indicator indicator, CreateIndicatorDTO createDTO) {
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
        // Get indicator details before deletion for auto-normalization
        Indicator indicator = findById(id);
        Long dimensionId = indicator.getDimension().getId();
        
        // Get all years that have weights for this indicator
        List<Integer> yearsToNormalize = indicator.getWeights().stream()
                .map(IndicatorWeight::getYear)
                .distinct()
                .toList();
        
        // Delete the indicator
        delete(id);
        
        // Disabled auto-normalization to preserve user-set weights
        // Auto-normalize weights for each affected dimension/year combination
        // for (Integer year : yearsToNormalize) {
        //     normalizeWeightsForDimensionYear(dimensionId, year);
        //     log.info("Auto-normalized weights for dimension {} year {} after deleting indicator {}", 
        //             dimensionId, year, indicator.getName());
        // }
    }

    public List<Indicator> findByDimensionId(Long dimensionId) {
        return indicatorRepository.findByDimensionId(dimensionId);
    }

    public Double getWeightByYear(Long indicatorId, Integer year) {
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
     */
    @Transactional
    public void normalizeWeightsForDimensionYear(Long dimensionId, Integer year) {
        List<Indicator> indicators = indicatorRepository.findByDimensionId(dimensionId);
        
        // Calculate current total weight for this year
        double totalWeight = indicators.stream()
                .mapToDouble(indicator -> {
                    if (indicator.getWeights() == null) return 0.0;
                    return indicator.getWeights().stream()
                            .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                            .mapToDouble(weight -> weight.getWeight() != null ? weight.getWeight() : 0.0)
                            .sum();
                })
                .sum();
        
        // Normalize proportionally to sum to 100% (only if there are weights and total > 0)
        if (totalWeight > 0.0) {
            // Scale factor to make total = 1.0 (since we store weights as decimals 0.0-1.0)
            double scaleFactor = 1.0 / totalWeight;
            
            indicators.forEach(indicator -> {
                if (indicator.getWeights() != null) {
                    indicator.getWeights().stream()
                            .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                            .forEach(weight -> {
                                if (weight.getWeight() != null) {
                                    double normalizedWeight = weight.getWeight() * scaleFactor;
                                    weight.setWeight(normalizedWeight);
                                }
                            });
                }
            });
            
            indicatorRepository.saveAll(indicators);
            log.info("Normalized weights for dimension {} year {} by factor {} (total was {}% -> 100%)", 
                    dimensionId, year, scaleFactor, totalWeight * 100);
        } else {
            log.warn("No weights found for dimension {} year {} - nothing to normalize", dimensionId, year);
        }
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
    private void validateWeightLimit(Long dimensionId, Integer year, Double newWeight, Long excludeIndicatorId) {
        try {
            // Get all indicators for this dimension
            List<Indicator> dimensionIndicators = indicatorRepository.findByDimensionId(dimensionId).stream()
                    .filter(indicator -> excludeIndicatorId == null || !indicator.getId().equals(excludeIndicatorId)) // Exclude current indicator if updating
                    .collect(Collectors.toList());
            
            // Calculate current total weight for this year
            double currentTotalWeight = dimensionIndicators.stream()
                    .mapToDouble(indicator -> {
                        if (indicator.getWeights() == null) return 0.0;
                        return indicator.getWeights().stream()
                                .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                                .mapToDouble(weight -> weight.getWeight() != null ? weight.getWeight() : 0.0)
                                .sum();
                    })
                    .sum();
            
            double proposedTotalWeight = currentTotalWeight + (newWeight != null ? newWeight : 0.0);
        
            // Throw an error if total would exceed 100%
            if (proposedTotalWeight > 1.0) { // 1.0 = 100%
                double currentPercentage = Math.round(currentTotalWeight * 1000.0) / 10.0; // Convert to percentage with 1 decimal
                double newPercentage = Math.round((newWeight != null ? newWeight : 0.0) * 1000.0) / 10.0;
                double proposedPercentage = Math.round(proposedTotalWeight * 1000.0) / 10.0;
                
                throw new CustomException(
                    String.format("Le poids total ne peut pas dépasser 100%%. " +
                                "Poids actuel de la dimension: %.1f%%, " +
                                "Poids maximum autorisé: %.1f%%", 
                                currentPercentage, 
                                (100.0 - currentPercentage)), 
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
    public double getDimensionWeightTotal(Long dimensionId, Integer year) {
        List<Indicator> dimensionIndicators = indicatorRepository.findByDimensionId(dimensionId);
        
        return dimensionIndicators.stream()
                .mapToDouble(indicator -> {
                    if (indicator.getWeights() == null) return 0.0;
                    return indicator.getWeights().stream()
                            .filter(weight -> weight != null && weight.getYear() != null && weight.getYear().equals(year))
                            .mapToDouble(weight -> weight.getWeight() != null ? weight.getWeight() : 0.0)
                            .sum();
                })
                .sum();
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

        // Create weightsByYear map from indicator weights
        Map<Integer, Double> weightsByYear = indicator.getWeights().stream()
                .collect(Collectors.toMap(
                    IndicatorWeight::getYear,
                    IndicatorWeight::getWeight
                )
        );

        // Calculate effective weights (dimension weight * indicator weight)
        Map<Integer, Double> effectiveWeightsByYear = new HashMap<>();
        Map<Integer, Double> dimensionWeightsByYear = new HashMap<>();
        
        for (IndicatorWeight indicatorWeight : indicator.getWeights()) {
            Integer year = indicatorWeight.getYear();
            Double indicatorWeightValue = indicatorWeight.getWeight();
            
            // Get dimension weight for this year
            var dimensionWeight = dimensionWeightService.findByCategoryAndYear(
                    indicator.getDimension().getId(), year);
            
            if (dimensionWeight != null && dimensionWeight.getDimensionWeight() != null) {
                Double dimensionWeightValue = dimensionWeight.getDimensionWeight();
                dimensionWeightsByYear.put(year, dimensionWeightValue);
                
                // Calculate effective weight
                if (indicatorWeightValue != null) {
                    effectiveWeightsByYear.put(year, indicatorWeightValue * dimensionWeightValue);
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