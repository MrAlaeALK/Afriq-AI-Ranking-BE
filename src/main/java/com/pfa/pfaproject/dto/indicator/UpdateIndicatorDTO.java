package com.pfa.pfaproject.dto.indicator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateIndicatorDTO(
        @NotBlank(message = "Le nom de l'indicateur est obligatoire")
        @Size(min = 3, max = 30, message = "Le nom doit contenir entre 2 et 30 caractères")
        String name,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 250, message = "La description doit contenir au plus 250 caractères")
        String description,

        Integer year,

        Long dimensionId,

        @DecimalMin(value = "0.0", message = "Le poids doit être supérieur ou égal à 0.0")
        @DecimalMax(value = "1.0", message = "Le poids doit être inférieur ou égal à 1.0")
        Double weight,

        String normalizationType
) {
}