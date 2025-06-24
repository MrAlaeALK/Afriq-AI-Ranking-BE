package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Score.getScoresByYearDTO;
import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.DimensionScore;
import com.pfa.pfaproject.model.Score;
import com.pfa.pfaproject.repository.DimensionScoreRepository;
import com.pfa.pfaproject.util.Utils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DimensionScoreService {
    private final DimensionScoreRepository dimensionScoreRepository;

    public DimensionScore save(DimensionScore dimensionScore) {
        dimensionScore.setScore(Utils.round(dimensionScore.getScore(), 2));
        return dimensionScoreRepository.save(dimensionScore);
    }

    public List<DimensionScore> findAll() {
        return dimensionScoreRepository.findAll();
    }


    public DimensionScore findByCountryIdAndDimensionIdAndYear(Long countryId, Long dimensionId, Integer  year) {
        return dimensionScoreRepository.findByCountry_IdAndDimension_IdAndYear(countryId, dimensionId, year);
    }

    public List<getScoresByYearDTO> findByYear(Integer  year){

        List<DimensionScore> scoreList= dimensionScoreRepository.findByYear(year);
        List<getScoresByYearDTO> responses= new ArrayList<>();
        for(DimensionScore score : scoreList){
            responses.add(new getScoresByYearDTO(score.getCountry().getName(), score.getDimension().getName(), score.getScore()));
        }
        return responses;
    }

    public List<DimensionScore> findScoresByYear(Integer year){
        return dimensionScoreRepository.findByYear(year);
    }

    public void delete(Long id) {
        if(!dimensionScoreRepository.existsById(id)){
            throw new CustomException("Dimension score not found", HttpStatus.NOT_FOUND);
        }
        dimensionScoreRepository.deleteById(id);
    }
}
