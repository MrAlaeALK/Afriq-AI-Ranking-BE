package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.DimensionWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DimensionWeightRepository extends JpaRepository<DimensionWeight, Long> {
    List<DimensionWeight> findByYear(Integer  year);

    DimensionWeight findByDimension_IdAndYear(Long id, Integer  year);
}
