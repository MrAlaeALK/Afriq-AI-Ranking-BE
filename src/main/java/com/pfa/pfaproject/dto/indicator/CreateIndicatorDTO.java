package com.pfa.pfaproject.dto.indicator;

import com.pfa.pfaproject.dto.Dimension.DimensionAssignmentDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
        @DecimalMin(value = "0.0", message = "Le poids doit être supérieur ou égal à 0.0")
        @DecimalMax(value = "1.0", message = "Le poids doit être inférieur ou égal à 1.0")
        Double weight,

        String normalizationType
) {
}
