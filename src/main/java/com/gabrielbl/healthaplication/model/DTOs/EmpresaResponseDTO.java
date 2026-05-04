package com.gabrielbl.healthaplication.model.DTOs;

import java.util.UUID;

public record EmpresaResponseDTO(UUID id, String cnpj, String nome, String email, String telefone) {
}
