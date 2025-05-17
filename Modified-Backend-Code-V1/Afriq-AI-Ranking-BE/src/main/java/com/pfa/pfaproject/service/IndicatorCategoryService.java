package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Indicator;
import com.pfa.pfaproject.model.IndicatorCategory;
import com.pfa.pfaproject.repository.IndicatorCategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing IndicatorCategory entities.
 * Handles category CRUD operations and relationship management with indicators.
 */
@Service
@AllArgsConstructor
@Slf4j
public class IndicatorCategoryService {
    private final IndicatorCategoryRepository indicatorCategoryRepository;

    /**
     * Returns all indicator categories in the system, ordered by display order.
     * @return List of all indicator categories
     */
    public List<IndicatorCategory> findAll() {
        return indicatorCategoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Finds an indicator category by ID.
     * @param id The indicator category ID
     * @return The found indicator category
     * @throws CustomException if indicator category is not found
     */
    public IndicatorCategory findById(Long id) {
        return indicatorCategoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Indicator category not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Saves an indicator category entity to the database.
     * @param category The indicator category to save
     * @return The saved indicator category with ID
     * @throws CustomException if validation fails
     */
    public IndicatorCategory save(IndicatorCategory category) {
        return indicatorCategoryRepository.save(category);
    }

    /**
     * Deletes an indicator category by ID.
     * @param id The indicator category ID to delete
     * @throws CustomException if indicator category is not found
     */
    public void delete(Long id) {
        IndicatorCategory category = findById(id);
        
        // Check if there are indicators assigned to this category
        if (!category.getIndicators().isEmpty()) {
            throw new CustomException(
                    "Cannot delete: Category has " + category.getIndicators().size() + " indicators assigned to it",
                    HttpStatus.CONFLICT);
        }

        indicatorCategoryRepository.deleteById(id);
    }

    /**
     * Finds an indicator category by name.
     * @param name The indicator category name to search
     * @return The found indicator category
     * @throws CustomException if indicator category is not found
     */
    public IndicatorCategory findByName(String name) {
        return indicatorCategoryRepository.findByName(name);
    }

    /**
     * Adds an indicator to a category.
     * @param categoryId The indicator category ID
     * @param indicator The indicator to add
     * @return The updated indicator category
     */
    @Transactional
    public IndicatorCategory addIndicator(Long categoryId, Indicator indicator) {
        IndicatorCategory category = findById(categoryId);
        category.addIndicator(indicator);
        return indicatorCategoryRepository.save(category);
    }

    /**
     * Removes an indicator from a category.
     * @param categoryId The indicator category ID
     * @param indicator The indicator to remove
     * @return The updated indicator category
     */
    public IndicatorCategory removeIndicator(Long categoryId, Indicator indicator) {
        IndicatorCategory category = findById(categoryId);
        category.removeIndicator(indicator);
        return indicatorCategoryRepository.save(category);
    }

    /**
     * Updates the display order of an indicator category.
     * @param id The indicator category ID
     * @param displayOrder The new display order
     * @return The updated indicator category
     */
    @Transactional
    public IndicatorCategory updateDisplayOrder(Long id, Integer displayOrder) {
        IndicatorCategory category = findById(id);
        category.setDisplayOrder(displayOrder);
        return indicatorCategoryRepository.save(category);
    }

    /**
     * Checks if an indicator category exists by ID.
     * @param id The indicator category ID to check
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return indicatorCategoryRepository.existsById(id);
    }

    /**
     * Checks if an indicator category exists by name.
     * @param name The indicator category name to check
     * @return true if exists, false otherwise
     */
    public boolean existsByName(String name) {
        return indicatorCategoryRepository.existsByName(name);
    }
} 