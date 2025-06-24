package com.pfa.pfaproject.dto.Admin;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank
        String refreshToken
) {
}
