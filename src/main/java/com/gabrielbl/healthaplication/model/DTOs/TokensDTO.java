package com.gabrielbl.healthaplication.model.DTOs;

public record TokensDTO(
        String accessToken,
        String refreshToken
) {
}
