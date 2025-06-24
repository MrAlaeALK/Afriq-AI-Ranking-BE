package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.DimensionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DimensionScoreRepository extends JpaRepository<DimensionScore, Long> {
    List<DimensionScore> findByYear(Integer  year);
    DimensionScore findByCountry_IdAndDimension_IdAndYear(Long country_id, Long dimension_id, Integer  year);
}
