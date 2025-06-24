package com.pfa.pfaproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScoresToValidateDTO(
        @NotBlank(message = "Le nom du pays doit être valide")
        String countryName,
        @NotBlank(message = "Le code du pays doit être valide")
        String countryCode,
        @NotNull
        Long indicatorId,
        @NotNull
        Double score
) {
}
