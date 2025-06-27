package com.pfa.pfaproject.dto.Dimension;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DimensionAssignmentDTO(
        @NotNull Integer year,
        @NotNull Long dimensionId,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double weight
) {}