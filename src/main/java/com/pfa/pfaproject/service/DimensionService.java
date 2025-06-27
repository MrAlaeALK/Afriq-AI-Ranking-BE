package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.CreateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.UpdateDimensionDTO;
import com.pfa.pfaproject.dto.Dimension.DimensionResponseDTO;
import com.pfa.pfaproject.dto.Dimension.GetDimensionsDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.model.Indicator;
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
        return dimensionRepository.findByName(name);
    }

    public Dimension findByNameAndYear(String name, Integer year) {
        return dimensionRepository.findByNameAndYear(name, year);
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
     * Delete a dimension by ID
     */
    @Transactional
    public void deleteDimension(Long id) {
        Dimension dimension = findById(id);
        dimensionRepository.delete(dimension);
        log.info("Successfully deleted dimension '{}' (ID: {})", dimension.getName(), id);
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
                .dimensionWeight(createDimensionDTO.weight()) // Already in decimal format from frontend
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
     * Update an existing dimension using DTO
     */
    @Transactional
    public DimensionResponseDTO updateDimension(Long id, UpdateDimensionDTO updateDimensionDTO) {
        Dimension dimension = findById(id);
        
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
        
        dimensionWeight.setDimensionWeight(updateDimensionDTO.weight()); // Already in decimal format from frontend
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