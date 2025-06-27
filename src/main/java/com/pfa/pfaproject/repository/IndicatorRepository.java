package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {
    Indicator findByName(String name);
    List<Indicator> findByDimensionId(Long dimensionId);
    long countByDimensionId(Long dimensionId);
}

