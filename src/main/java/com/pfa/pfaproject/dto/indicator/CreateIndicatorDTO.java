package com.pfa.pfaproject.dto.indicator;

import com.pfa.pfaproject.dto.Dimension.DimensionAssignmentDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record CreateIndicatorDTO(
        @NotBlank(message = "Le nom de l'indicateur est obligatoire")
        @Size(min = 3, max = 30, message = "Le nom doit contenir entre 2 et 30 caractères")
        String name,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 250, message = "La description doit contenir au plus 250 caractères")
        String description,

        @NotNull(message = "L'année est obligatoire")
        Integer year,

        @NotNull(message = "La dimension de l'indicateur est obligatoire")
        Long dimensionId,

        @NotNull(message = "Le poids est obligatoire")
        @Min(value = 1, message = "Le poids doit être supérieur ou égal à 1")
        @Max(value = 100, message = "Le poids doit être inférieur ou égal à 100")
        Integer weight,

        String normalizationType
) {
}
