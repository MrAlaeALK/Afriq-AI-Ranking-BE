package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.IndicatorWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndicatorWeightRepository extends JpaRepository<IndicatorWeight, Long> {
}
