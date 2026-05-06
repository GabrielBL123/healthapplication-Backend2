package com.gabrielbl.healthaplication.model.DTOs;

import java.util.UUID;

public record AvaliacaoMensalResponseDTO(
        UUID id,
        String competencia,
        Boolean status
) {}