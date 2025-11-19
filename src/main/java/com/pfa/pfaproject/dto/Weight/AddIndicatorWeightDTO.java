package com.pfa.pfaproject.dto.Weight;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddIndicatorWeightDTO(
        @NotNull(message = "L'ID de l'indicateur est obligatoire")
        Long indicatorId,
        
        Long categoryId,
        
        @NotNull(message = "Le poids est obligatoire")
        @Min(value = 1, message = "Le poids doit être supérieur ou égal à 1")
        @Max(value = 100, message = "Le poids doit être inférieur ou égal à 100")
        Integer weight,
        
        @NotNull(message = "L'année est obligatoire")
        Integer year
) {
}
