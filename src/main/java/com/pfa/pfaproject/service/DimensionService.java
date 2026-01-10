package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.CreateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.UpdateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.DimensionResponseDTO;
import com.pfa.pfaproject.dto.Dimension.GetDimensionsDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.Rank;
import com.pfa.pfaproject.repository.DimensionRepository;
import com.pfa.pfaproject.repository.DimensionWeightRepository;
import com.pfa.pfaproject.repository.IndicatorRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing IndicatorCategory entities.
 * Handles category CRUD operations and relationship management with indicators.
 */
@Service
@AllArgsConstructor
@Slf4j
public class DimensionService {
    private final DimensionRepository dimensionRepository;
    private final DimensionWeightRepository dimensionWeightRepository;
    private final IndicatorRepository indicatorRepository;
    private final RankService rankService;

    public List<Dimension> findAll() {
        return dimensionRepository.findAll();
    }

    public Dimension findById(Long id) {
        return dimensionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Indicator category not found", HttpStatus.NOT_FOUND));
    }

    public Dimension save(Dimension category) {
        return dimensionRepository.save(category);
    }

    public Dimension findByName(String name) {
        List<Dimension> dimensions = dimensionRepository.findByName(name);
        if (dimensions.isEmpty()) {
            return null;
        }
        if (dimensions.size() > 1) {
            log.warn("Found {} duplicate dimensions with name '{}'. Using the first one (ID: {}). Please clean up duplicates.", 
                    dimensions.size(), name, dimensions.get(0).getId());
        }
        return dimensions.get(0);
    }

    public Dimension findByNameAndYear(String name, Integer year) {
        List<Dimension> dimensions = dimensionRepository.findByNameAndYear(name, year);
        if (dimensions.isEmpty()) {
            return null;
        }
        if (dimensions.size() > 1) {
            log.warn("Found {} duplicate dimensions with name '{}' and year {}. Using the first one (ID: {}). Please clean up duplicates.", 
                    dimensions.size(), name, year, dimensions.get(0).getId());
        }
        return dimensions.get(0);
    }

    public boolean existsById(Long id) {
        return dimensionRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return dimensionRepository.existsByName(name);
    }

    public boolean existsByNameAndYear(String name, Integer year) {
        return dimensionRepository.existsByNameAndYear(name, year);
    }

    public List<GetDimensionsDTO> getAllDimensions() {
        List<Dimension> dimensions = dimensionRepository.findAll();
        List<GetDimensionsDTO> dimensionsList = new ArrayList<>();
        
        for (Dimension dimension : dimensions) {
            // For each dimension, get all its dimension weights (one per year)
            for (DimensionWeight weight : dimension.getWeights()) {
                dimensionsList.add(new GetDimensionsDTO(
                    dimension.getId(), 
                    dimension.getName(), 
                    dimension.getDescription(),
                    weight.getYear()
                ));
            }
        }

        return dimensionsList;
    }

    /**
     * Delete a dimension by ID with ranking validation
     */
    @Transactional
    public void deleteDimension(Long id) {
        Dimension dimension = findById(id);
        
        // Check if rankings exist for this dimension's year
        List<Integer> affectedYears = dimension.getWeights().stream()
                .map(DimensionWeight::getYear)
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
                String.format("Cette dimension est utilisée dans les classements %s.", yearsList),
                HttpStatus.CONFLICT
            );
        }
        
        dimensionRepository.delete(dimension);
        log.info("Successfully deleted dimension '{}' (ID: {})", dimension.getName(), id);
    }

    /**
     * Force delete a dimension by ID (bypasses ranking validation)
     */
    @Transactional
    public void forceDeleteDimension(Long id) {
        Dimension dimension = findById(id);
        dimensionRepository.delete(dimension);
        log.info("Force deleted dimension '{}' (ID: {}) - rankings may need regeneration", dimension.getName(), id);
    }

    /**
     * Bulk delete dimensions by IDs with ranking validation
     */
    @Transactional
    public void bulkDeleteDimensions(List<Long> dimensionIds) {
        List<Integer> allAffectedYears = new ArrayList<>();
        
        // Check all dimensions first before deleting any
        for (Long id : dimensionIds) {
            Dimension dimension = findById(id);
            
            // Check if rankings exist for this dimension's year
            List<Integer> affectedYears = dimension.getWeights().stream()
                    .map(DimensionWeight::getYear)
                    .distinct()
                    .toList();
            
            for (Integer year : affectedYears) {
                List<Rank> rankingsForYear = rankService.findAllByYear(year);
                if (!rankingsForYear.isEmpty()) {
                    allAffectedYears.add(year);
                }
            }
        }
        
        if (!allAffectedYears.isEmpty()) {
            String yearsList = allAffectedYears.stream()
                    .distinct()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
                    
            throw new CustomException(
                String.format("Ces dimensions sont utilisées dans les classements %s.", yearsList),
                HttpStatus.CONFLICT
            );
        }
        
        // Delete all dimensions if validation passes
        for (Long id : dimensionIds) {
            Dimension dimension = findById(id);
            dimensionRepository.delete(dimension);
            log.info("Successfully deleted dimension '{}' (ID: {})", dimension.getName(), id);
        }
    }

    /**
     * Create a new dimension using DTO - allows same name for different years
     */
    @Transactional
    public DimensionResponseDTO createDimension(CreateDimensionDTO createDimensionDTO) {
        // Check if dimension with same name already exists for this specific year
        boolean dimensionExists = dimensionRepository.existsByNameAndYear(
            createDimensionDTO.name(), 
            createDimensionDTO.year()
        );
        
        if (dimensionExists) {
            throw new CustomException(
                String.format("Une dimension avec le nom '%s' existe déjà pour l'année %d", 
                    createDimensionDTO.name(), createDimensionDTO.year()), 
                HttpStatus.CONFLICT
            );
        }
        
        // Check if rankings exist for this year
        List<Rank> rankingsForYear = rankService.findAllByYear(createDimensionDTO.year());
        if (!rankingsForYear.isEmpty()) {
            throw new CustomException(
                String.format("L'ajout de cette dimension invalidera les classements existants pour %d.", 
                    createDimensionDTO.year()),
                HttpStatus.CONFLICT
            );
        }
        
        // Create new dimension with the year
        Dimension dimension = Dimension.builder()
                .name(createDimensionDTO.name())
                .description(createDimensionDTO.description())
                .year(createDimensionDTO.year())
                .build();
        
        Dimension savedDimension = dimensionRepository.save(dimension);

        // Create dimension weight for the specified year
        DimensionWeight dimensionWeight = DimensionWeight.builder()
                .dimension(savedDimension)
                .year(createDimensionDTO.year())
                .dimensionWeight(createDimensionDTO.weight()) // Direct assignment, no conversion needed
                .build();
        
        dimensionWeightRepository.save(dimensionWeight);

        return new DimensionResponseDTO(
                savedDimension.getId(),
                savedDimension.getName(),
                savedDimension.getDescription(),
                createDimensionDTO.weight(),
                createDimensionDTO.year()
        );
    }

    /**
     * Force create a new dimension (bypasses ranking validation)
     */
    @Transactional
    public DimensionResponseDTO forceCreateDimension(CreateDimensionDTO createDimensionDTO) {
        // Check if dimension with same name already exists for this specific year
        boolean dimensionExists = dimensionRepository.existsByNameAndYear(
            createDimensionDTO.name(), 
            createDimensionDTO.year()
        );
        
        if (dimensionExists) {
            throw new CustomException(
                String.format("Une dimension avec le nom '%s' existe déjà pour l'année %d", 
                    createDimensionDTO.name(), createDimensionDTO.year()), 
                HttpStatus.CONFLICT
            );
        }
        
        // Create new dimension with the year (skip ranking validation)
        Dimension dimension = Dimension.builder()
                .name(createDimensionDTO.name())
                .description(createDimensionDTO.description())
                .year(createDimensionDTO.year())
                .build();
        
        Dimension savedDimension = dimensionRepository.save(dimension);

        // Create dimension weight for the specified year
        DimensionWeight dimensionWeight = DimensionWeight.builder()
                .dimension(savedDimension)
                .year(createDimensionDTO.year())
                .dimensionWeight(createDimensionDTO.weight())
                .build();
        
        dimensionWeightRepository.save(dimensionWeight);

        log.info("Force created dimension '{}' for year {} - existing rankings may be invalidated", 
                savedDimension.getName(), createDimensionDTO.year());

        return new DimensionResponseDTO(
                savedDimension.getId(),
                savedDimension.getName(),
                savedDimension.getDescription(),
                createDimensionDTO.weight(),
                createDimensionDTO.year()
        );
    }

    /**
     * Update an existing dimension using DTO
     */
    @Transactional
    public DimensionResponseDTO updateDimension(Long id, UpdateDimensionDTO updateDimensionDTO) {
        Dimension dimension = findById(id);
        
        // Get current weight for the year being updated
        DimensionWeight currentWeight = dimension.getWeights().stream()
                .filter(w -> w.getYear().equals(updateDimensionDTO.year()))
                .findFirst()
                .orElse(null);
        Integer currentWeightValue = currentWeight != null ? currentWeight.getDimensionWeight() : null;
        
        // Check if anything affecting the ranking actually changes
        boolean yearChanged = !dimension.getYear().equals(updateDimensionDTO.year());
        boolean weightChanged = currentWeightValue == null || !updateDimensionDTO.weight().equals(currentWeightValue);

        // Only check rankings if something that affects ranking changes (year and weight)
        boolean hasRankingAffectingChanges = yearChanged || weightChanged;

        // Only check rankings if ranking-affecting changes occur
        if (hasRankingAffectingChanges) {
            // Check if rankings exist for this dimension's years (before update)
            List<Integer> affectedYears = dimension.getWeights().stream()
                    .map(DimensionWeight::getYear)
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
                    String.format("Cette dimension est utilisée dans les classements %s. Veuillez d'abord supprimer les classements associés.", yearsList),
                    HttpStatus.CONFLICT
                );
            }
        }
        
        // Check if another dimension with same name exists for this year (excluding current dimension)
        Dimension existingDimension = findByNameAndYear(updateDimensionDTO.name(), updateDimensionDTO.year());
        if (existingDimension != null && !existingDimension.getId().equals(id)) {
            throw new CustomException(
                String.format("Une dimension avec le nom '%s' existe déjà pour l'année %d", 
                    updateDimensionDTO.name(), updateDimensionDTO.year()), 
                HttpStatus.CONFLICT
            );
        }
        
        // Update dimension info
        dimension.setName(updateDimensionDTO.name());
        dimension.setDescription(updateDimensionDTO.description());
        dimension.setYear(updateDimensionDTO.year());
        
        Dimension savedDimension = dimensionRepository.save(dimension);

        // Update dimension weight for the specified year
        DimensionWeight dimensionWeight = savedDimension.getWeights().stream()
                .filter(w -> w.getYear().equals(updateDimensionDTO.year()))
                .findFirst()
                .orElse(DimensionWeight.builder()
                        .dimension(savedDimension)
                        .year(updateDimensionDTO.year())
                        .build());
        
        dimensionWeight.setDimensionWeight(updateDimensionDTO.weight()); // Direct assignment, no conversion needed
        dimensionWeightRepository.save(dimensionWeight);

        return new DimensionResponseDTO(
                savedDimension.getId(),
                savedDimension.getName(),
                savedDimension.getDescription(),
                updateDimensionDTO.weight(),
                updateDimensionDTO.year()
        );
    }
} 