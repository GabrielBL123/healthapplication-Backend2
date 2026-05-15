package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record AvaliacaoMensalDTO(@NotBlank String competencia, @NotBlank String cnpj) {
}
