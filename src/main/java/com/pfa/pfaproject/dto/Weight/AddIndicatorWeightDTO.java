package com.pfa.pfaproject.dto.Weight;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddIndicatorWeightDTO(
        @NotNull(message = "L'ID de l'indicateur est obligatoire")
        Long indicatorId,
        
        Long categoryId,
        
        @NotNull(message = "Le poids est obligatoire")
        @DecimalMin(value = "0.0", message = "Le poids doit être supérieur ou égal à 0.0")
        @DecimalMax(value = "1.0", message = "Le poids doit être inférieur ou égal à 1.0")
        Double weight,
        
        @NotNull(message = "L'année est obligatoire")
        Integer year
) {
}
