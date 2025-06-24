package com.pfa.pfaproject.dto.Score;

import jakarta.validation.constraints.*;

public record ScoreDTO(
        @NotNull
        Long id,

        @NotNull(message = "l'année doit être valide")
        Integer year,

        @NotNull(message = "le nom du pays doit être valide")
        @NotBlank(message = "Le nom du pays ne doit pas être vide")
        String countryName,

        @NotNull(message = "L'indicateur doit être valide")
        @NotBlank(message = "L'indicateur ne doit pas être vide")
        String indicatorName,

        @NotNull(message = "Le score doit ne doit pas être nul")
        @Min(value = 0, message = "Le score doit être au moins 0")
        @Max(value = 100, message = "Le score doit être au plus 100")
        Double score
) {
}
