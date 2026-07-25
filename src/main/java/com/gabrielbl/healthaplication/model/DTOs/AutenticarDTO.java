package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;
import java.util.Optional;

public record AutenticarDTO(
        String accessToken,
        String refreshToken,
        List<String> roles,
        String nome,
        String login,
        String empresaNome,
        String empresaID,
        String usuarioID,
        String avaliacaoAtivaId
) {
}
