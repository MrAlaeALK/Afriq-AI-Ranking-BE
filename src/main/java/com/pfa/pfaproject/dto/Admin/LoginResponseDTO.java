package com.pfa.pfaproject.dto.Admin;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken
) {
}
