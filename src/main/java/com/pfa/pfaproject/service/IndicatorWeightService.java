package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.indicator.GetYearIndicatorsDTO;
import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.repository.IndicatorWeightRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class IndicatorWeightService {
    private final IndicatorWeightRepository indicatorWeightRepository;

    public IndicatorWeight save(IndicatorWeight indicatorWeight){
        return indicatorWeightRepository.save(indicatorWeight);
    }

    public List<IndicatorWeight> findAll(){
        return indicatorWeightRepository.findAll();
    }

    public List<GetYearIndicatorsDTO> getYearIndicators(Integer  year){
        log.info("Searching for indicators for year: {}", year);
        List<IndicatorWeight> yearIndicatorsWeights = indicatorWeightRepository.findAllByDimensionWeight_Year(year);
        log.info("Found {} indicator weights for year {}", yearIndicatorsWeights.size(), year);
        
        List<GetYearIndicatorsDTO> yearIndicatorsDTOs = new ArrayList<>();

        for( IndicatorWeight indicatorWeight : yearIndicatorsWeights ){
            
            if (indicatorWeight.getIndicator() != null && 
                indicatorWeight.getDimensionWeight() != null && 
                indicatorWeight.getDimensionWeight().getDimension() != null) {
                yearIndicatorsDTOs.add(new GetYearIndicatorsDTO(
                    indicatorWeight.getIndicator().getId(), 
                    indicatorWeight.getIndicator().getName(), 
                    indicatorWeight.getIndicator().getDescription(), 
                    indicatorWeight.getWeight(), 
                    indicatorWeight.getDimensionWeight().getDimension().getName()
                ));
        }
        }
        
        log.info("Returning {} indicators for year {}", yearIndicatorsDTOs.size(), year);
        return yearIndicatorsDTOs;
    }
}
