package com.pfa.pfaproject.service;

import com.pfa.pfaproject.model.IndicatorWeight;
import com.pfa.pfaproject.repository.IndicatorWeightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IndicatorWeightService {
    private final IndicatorWeightRepository indicatorWeightRepository;

    public IndicatorWeight save(IndicatorWeight indicatorWeight){
        return indicatorWeightRepository.save(indicatorWeight);
    }
}
