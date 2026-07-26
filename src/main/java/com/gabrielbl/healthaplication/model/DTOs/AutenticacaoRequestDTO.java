package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequestDTO(
        @NotBlank String login,
        @NotBlank String password
) {
}