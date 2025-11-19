package com.pfa.pfaproject.dto.Dimension;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DimensionAssignmentDTO(
        @NotNull Integer year,
        @NotNull Long dimensionId,
        @NotNull @Min(1) @Max(100) Integer weight
) {}