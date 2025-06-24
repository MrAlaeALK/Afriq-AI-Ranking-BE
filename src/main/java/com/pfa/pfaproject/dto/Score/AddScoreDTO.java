package com.pfa.pfaproject.dto.Score;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddScoreDTO(
        @NotNull(message = "L'année est requise")
        Integer year,
        @NotNull(message = "Le nom du pays est requis") @NotBlank(message = "Le nom du pays ne doit pas être vide")
        String countryName,
        @NotNull(message = "Le nom de l'indicateur est requis") @NotBlank(message = "Le nom de l'indicateur ne doit pas être vide")
        String indicatorName,
        @NotNull(message = "Le score est requis")
        @Min(value = 0, message = "Le score doit être au moins 0")
        @Max(value = 100, message = "Le score doit être au plus 100")
        Double score
) {
}
