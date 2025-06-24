package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.indicator.GetYearIndicatorsDTO;
import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.repository.IndicatorWeightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class IndicatorWeightService {
    private final IndicatorWeightRepository indicatorWeightRepository;

    public IndicatorWeight save(IndicatorWeight indicatorWeight){
        return indicatorWeightRepository.save(indicatorWeight);
    }

    public List<IndicatorWeight> findAll(){
        return indicatorWeightRepository.findAll();
    }

    public List<GetYearIndicatorsDTO> getYearIndicators(Integer  year){
        List<IndicatorWeight> yearIndicatorsWeights = indicatorWeightRepository.findAllByDimensionWeight_Year(year);
        List<GetYearIndicatorsDTO> yearIndicatorsDTOs = new ArrayList<>();
        for( IndicatorWeight indicatorWeight : yearIndicatorsWeights ){
            yearIndicatorsDTOs.add(new GetYearIndicatorsDTO(indicatorWeight.getIndicator().getId(), indicatorWeight.getIndicator().getName(), indicatorWeight.getIndicator().getDescription(), indicatorWeight.getWeight(), indicatorWeight.getDimensionWeight().getDimension().getName()));
        }
        return yearIndicatorsDTOs;
    }
}
