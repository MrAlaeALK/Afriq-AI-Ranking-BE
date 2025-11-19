package com.pfa.pfaproject.dto.Dimension;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDimensionDTO(
        @NotBlank(message = "Le nom de la dimension est obligatoire")
        @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
        String name,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 500, message = "La description doit contenir au plus 500 caractères")
        String description,

        @NotNull(message = "Le poids est obligatoire")
        @Min(value = 1, message = "Le poids doit être supérieur ou égal à 1")
        @Max(value = 100, message = "Le poids doit être inférieur ou égal à 100")
        Integer weight,

        @NotNull(message = "L'année est obligatoire")
        Integer year
) {
} 