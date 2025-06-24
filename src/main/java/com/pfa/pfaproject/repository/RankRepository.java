package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    List<Rank> findAllByYearOrderByFinalScoreDesc(Integer  year);
    Rank findByCountry_IdAndYear(Long countryId, Integer  year);
    List<Rank> findAllByYearOrderByRank(Integer  year);
    List<Rank> findAllByYear(Integer  year);
}