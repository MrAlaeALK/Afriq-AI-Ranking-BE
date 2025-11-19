package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.indicator.GetYearIndicatorsDTO;
import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.repository.IndicatorWeightRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

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

    public void deleteByDimensionIdAndYear(Long dimensionId, Integer year) {
        indicatorWeightRepository.deleteByDimensionIdAndYear(dimensionId, year);
    }

    public List<IndicatorWeight> findByDimensionIdAndYear(Long dimensionId, Integer year) {
        return indicatorWeightRepository.findByDimensionIdAndYear(dimensionId, year);
    }

    public List<GetYearIndicatorsDTO> getYearIndicators(Integer  year){
        log.info("Searching for indicators for year: {}", year);
        List<IndicatorWeight> yearIndicatorsWeights = indicatorWeightRepository.findAllByDimensionWeight_Year(year);
        log.info("Found {} indicator weights for year {}", yearIndicatorsWeights.size(), year);
        
        List<GetYearIndicatorsDTO> yearIndicatorsDTOs = new ArrayList<>();
        Set<String> seenIndicatorNames = new LinkedHashSet<>(); // Use LinkedHashSet to maintain order and avoid duplicates

        for( IndicatorWeight indicatorWeight : yearIndicatorsWeights ){
            
            if (indicatorWeight.getIndicator() != null && 
                indicatorWeight.getDimensionWeight() != null && 
                indicatorWeight.getDimensionWeight().getDimension() != null) {
                
                String indicatorName = indicatorWeight.getIndicator().getName();
                
                // Only add if we haven't seen this indicator name before
                if (!seenIndicatorNames.contains(indicatorName)) {
                    seenIndicatorNames.add(indicatorName);
                    yearIndicatorsDTOs.add(new GetYearIndicatorsDTO(
                        indicatorWeight.getIndicator().getId(), 
                        indicatorName, 
                        indicatorWeight.getIndicator().getDescription(), 
                        indicatorWeight.getWeight(), 
                        indicatorWeight.getDimensionWeight().getDimension().getName()
                    ));
                }
            }
        }
        
        log.info("Returning {} unique indicators for year {} (filtered from {} total)", 
                yearIndicatorsDTOs.size(), year, yearIndicatorsWeights.size());
        return yearIndicatorsDTOs;
    }
}
