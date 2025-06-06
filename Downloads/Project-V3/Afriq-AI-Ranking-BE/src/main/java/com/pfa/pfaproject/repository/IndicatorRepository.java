package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, Long> {
    public Indicator findByName(String name);
    List<Indicator> findByNormalizationType(String normalizationType);
//    List<Indicator> findByDimension_Id(Long categoryId);
}

