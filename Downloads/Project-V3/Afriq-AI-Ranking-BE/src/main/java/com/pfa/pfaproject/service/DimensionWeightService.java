package com.pfa.pfaproject.service;

import com.pfa.pfaproject.dto.Dimension.GetYearDimensionsDTO;
import com.pfa.pfaproject.model.DimensionWeight;
import com.pfa.pfaproject.repository.DimensionWeightRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DimensionWeightService {
    private final DimensionWeightRepository dimensionWeightRepository;

    public List<DimensionWeight> getAllByYear(int year) {
        return dimensionWeightRepository.findByYear(year);
    }

    public DimensionWeight save(DimensionWeight dimensionWeight) {
        return dimensionWeightRepository.save(dimensionWeight);
    }

    public DimensionWeight findByCategoryAndYear(Long dimensionId, int year) {
        return dimensionWeightRepository.findByDimension_IdAndYear(dimensionId, year);
    }

    public List<DimensionWeight> findByYear(int year) {
        return dimensionWeightRepository.findByYear(year);
    }

    public List<GetYearDimensionsDTO> getYearDimensions(int year) {
        List<DimensionWeight> dimensionWeights = dimensionWeightRepository.findByYear(year);
        List<GetYearDimensionsDTO> yearDimensions = new ArrayList<>();
        for (DimensionWeight dimensionWeight : dimensionWeights) {
            yearDimensions.add(new GetYearDimensionsDTO(dimensionWeight.getDimension().getId(), dimensionWeight.getDimension().getName(), dimensionWeight.getDimension().getDescription(), dimensionWeight.getDimensionWeight()));
        }
        return yearDimensions;
    }
}
