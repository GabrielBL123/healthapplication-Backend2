package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record AvaliacaoMensalResponseDTO(
        String id,
        String competencia,
        Boolean status,
        String cnpj
) {}