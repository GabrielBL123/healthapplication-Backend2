package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoDTO(@NotBlank String login, @NotBlank String password) {
}