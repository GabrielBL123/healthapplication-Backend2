package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public record RegistrarDTO(@NotBlank String nome, @NotBlank String login, @NotBlank String password,
                           @NotNull UsuarioFuncao role,
                           String empresaCnpj,
                           String cargo,
                           LocalDateTime tempoDeTrabalho,
                           Duration jornada) {
}
