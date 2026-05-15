package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record RegistrarSetorDTO(@NotBlank String setor, @NotBlank String cnpj) {
}
