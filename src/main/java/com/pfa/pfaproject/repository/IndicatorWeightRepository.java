package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.IndicatorWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Year;
import java.util.List;

@Repository
public interface IndicatorWeightRepository extends JpaRepository<IndicatorWeight, Long> {
    List<IndicatorWeight> findAllByDimensionWeight_Year(Integer  year);
}
