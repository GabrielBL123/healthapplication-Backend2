package com.gabrielbl.healthaplication.model.DTOs;

public record TokenPairDTO(
        String accessToken,
        String refreshToken
) {
}
