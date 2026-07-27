package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;

public record PerfilDTO(
        List<String> roles,
        String nome,
        String login,
        String empresaNome,
        String empresaID,
        String usuarioID,
        String avaliacaoAtivaId
) {
}
