package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;
import java.util.UUID;

public record EmpresaResponseDTO(
        UUID id, // Ou String, dependendo de como você mapeou
        String cnpj,
        String nome,
        String email,
        String telefone,
        List<SetorResponseDTO> setores // Essa linha é obrigatória agora!
) {
}