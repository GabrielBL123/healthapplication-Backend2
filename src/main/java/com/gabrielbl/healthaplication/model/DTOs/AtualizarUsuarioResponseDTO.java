package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import jakarta.validation.constraints.NotBlank;

import java.time.Duration;
import java.time.LocalDateTime;

public record AtualizarUsuarioResponseDTO(@NotBlank String login, String nome, UsuarioFuncao role,
                                          String password,
                                          String cargo, String nomeSetor, LocalDateTime tempoDeTrabalho,
                                          Duration jornada, String empresaNome) {
}
