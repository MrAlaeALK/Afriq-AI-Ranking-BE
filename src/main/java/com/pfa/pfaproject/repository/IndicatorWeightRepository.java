package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.IndicatorWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Repository
public interface IndicatorWeightRepository extends JpaRepository<IndicatorWeight, Long> {
    List<IndicatorWeight> findAllByDimensionWeight_Year(Integer  year);
    
    @Query("SELECT iw FROM IndicatorWeight iw WHERE iw.dimensionWeight.dimension.id = :dimensionId AND iw.dimensionWeight.year = :year")
    List<IndicatorWeight> findByDimensionIdAndYear(@Param("dimensionId") Long dimensionId, @Param("year") Integer year);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM IndicatorWeight iw WHERE iw.dimensionWeight.dimension.id = :dimensionId AND iw.dimensionWeight.year = :year")
    void deleteByDimensionIdAndYear(@Param("dimensionId") Long dimensionId, @Param("year") Integer year);
}
