package com.pfa.pfaproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ValidatedScoresDTO(
        @NotNull
        Integer  year,
        @Valid
        List<ScoresToValidateDTO> validatedScores
) {
}
