package com.gabrielbl.healthaplication.model.DTOs;

import java.util.UUID;

public record SetorResponseDTO(UUID id, String nome, UUID empresaId, String empresaNome) {
}
