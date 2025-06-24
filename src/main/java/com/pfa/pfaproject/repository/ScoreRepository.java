package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Score findByCountry_IdAndIndicator_IdAndYear(Long countryId, Long indicatorId, Integer  year);
    Score findByCountry_NameAndIndicator_NameAndYear(String countryName, String indicatorName, Integer  year);
}


