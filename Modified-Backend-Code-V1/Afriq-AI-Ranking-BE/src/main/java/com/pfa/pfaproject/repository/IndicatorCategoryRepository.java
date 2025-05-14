package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.IndicatorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorCategoryRepository extends JpaRepository<IndicatorCategory, Long> {
    IndicatorCategory findByName(String name);
    List<IndicatorCategory> findAllByOrderByDisplayOrderAsc();
    boolean existsByName(String name);
} 