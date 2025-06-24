package com.pfa.pfaproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WantedColumnsDTO(
        @NotNull(message = "la colonne des l'indicateur doit être valide") @NotBlank(message = "La colonne de l'indicateur ne doit pas être vide")
        String countryColumn,
        @NotNull(message = "La liste des indicateurs ne doit pas être vide")
        @Size(min = 1, message = "Au moins un indicateur doit être fourni")
        @Valid
        List<IndicatorColumn> indicatorColumns,
        @NotNull(message = "l'année doit être valide")
        Integer year,
        @NotNull(message = "Le choix de normalization doit être valide")
        Boolean isNormalized
) {
    public record IndicatorColumn(
            @NotNull(message = "la colonne des l'indicateur doit être valide") @NotBlank(message = "La colonne de l'indicateur ne doit pas être vide")
            String columnName,
            @NotNull(message = "l'indicateur sélectionné doit être valide")
            @NotBlank(message = "L'identifiant de l'indicateur ne doit pas être vide")
            String indicatorId
    ) {}
}
