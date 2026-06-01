package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public record FuncionarioDTO(
        String login,
        String nome,
        String setor,
        String cargo,
        LocalDateTime tempoDeTrabalho,
        Duration jornada
) {
}
