package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    public Score findByCountry_IdAndIndicator_IdAndYear(Long countryId, Long indicatorId, int year);
    public Score findByYear(int year);
    List<Score> findByIndicator_IdAndYear(Long indicatorId, int year);
    List<Score> findByCountry_IdAndYear(Long countryId, int year);
}


