package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.GetDimensionsDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Dimension;
import com.pfa.pfaproject.repository.DimensionRepository;
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

    /**
     * Returns all indicator categories in the system, ordered by display order.
     * @return List of all indicator categories
     */
//    public List<Dimension> findAll() {
//        return dimensionRepository.findAllByOrderByDisplayOrderAsc();
//    }

    public List<Dimension> findAll() {
        return dimensionRepository.findAll();
    }
    /**
     * Finds an indicator category by ID.
     * @param id The indicator category ID
     * @return The found indicator category
     * @throws CustomException if indicator category is not found
     */
    public Dimension findById(Long id) {
        return dimensionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Indicator category not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Saves an indicator category entity to the database.
     * @param category The indicator category to save
     * @return The saved indicator category with ID
     * @throws CustomException if validation fails
     */
    public Dimension save(Dimension category) {
        return dimensionRepository.save(category);
    }

    /**
     * Deletes an indicator category by ID.
     * @param id The indicator category ID to delete
     * @throws CustomException if indicator category is not found
     */
//    public void delete(Long id) {
//        Dimension dimension = findById(id);
//
//        // Check if there are indicators assigned to this dimension
//        if (!dimension.getIndicators().isEmpty()) {
//            throw new CustomException(
//                    "Cannot delete: Category has " + dimension.getIndicators().size() + " indicators assigned to it",
//                    HttpStatus.CONFLICT);
//        }
//
//        dimensionRepository.deleteById(id);
//    }

    /**
     * Finds an indicator category by name.
     * @param name The indicator category name to search
     * @return The found indicator category
     * @throws CustomException if indicator category is not found
     */
    public Dimension findByName(String name) {
        return dimensionRepository.findByName(name);
    }


    /**
     * Updates the display order of an indicator category.
     * @param id The indicator category ID
     * @param displayOrder The new display order
     * @return The updated indicator category
     */
    @Transactional
    public Dimension updateDisplayOrder(Long id, Integer displayOrder) {
        Dimension category = findById(id);
        category.setDisplayOrder(displayOrder);
        return dimensionRepository.save(category);
    }

    /**
     * Checks if an indicator category exists by ID.
     * @param id The indicator category ID to check
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return dimensionRepository.existsById(id);
    }

    /**
     * Checks if an indicator category exists by name.
     * @param name The indicator category name to check
     * @return true if exists, false otherwise
     */
    public boolean existsByName(String name) {
        return dimensionRepository.existsByName(name);
    }

    public List<GetDimensionsDTO> getAllDimensions() {
        List<Dimension> dimensions = dimensionRepository.findAll();
        List<GetDimensionsDTO> dimensionsList = new ArrayList<>();
        for (Dimension dimension : dimensions) {
            dimensionsList.add(new GetDimensionsDTO(dimension.getId(), dimension.getName(), dimension.getDescription()));
        }

        return dimensionsList;
    }
} 