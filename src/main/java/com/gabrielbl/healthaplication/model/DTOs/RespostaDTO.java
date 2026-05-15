package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDateTime;

public record RespostaDTO(@NotBlank String nome,
                          @NotBlank String login,
                          @NotBlank String password,
                          @NotBlank String cargo,
                          @NotBlank String setor,
                          @NotNull LocalDateTime tempoDeTrabalho,
                          @NotNull Duration jornada,
                          @NotNull @Size(min = 27, max = 27) int[] resposta) {
}
