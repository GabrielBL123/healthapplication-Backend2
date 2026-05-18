package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public record ListaRespostaDTO(@NotBlank String nome,
                               @NotBlank String login,

                               @NotBlank String cargo,
                               @NotBlank String setor,
                               @NotNull LocalDateTime tempoDeTrabalho,
                               @NotNull Duration jornada,
                               @NotNull LocalDateTime submetidoEm


                              ){
}
