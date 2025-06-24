package com.pfa.pfaproject.dto;

import jakarta.validation.constraints.NotNull;

public record YearRequestDTO(
        @NotNull(message = "L'année est requise")
        Integer year
) {
}
