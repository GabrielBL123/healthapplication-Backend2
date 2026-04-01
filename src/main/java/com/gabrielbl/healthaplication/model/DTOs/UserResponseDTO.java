package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;

import java.time.Duration;
import java.time.LocalDateTime;

public record UserResponseDTO(String login, String nome, String role,
                              String cargo, String nomeSetor, LocalDateTime tempoDeTrabalho,
                              Duration jornada, String empresaNome) {



}
