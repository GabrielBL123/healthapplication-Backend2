package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record AtualizarEmpresaDTO(@NotBlank String nome,
                                  @NotBlank String email,
                                  @NotBlank String telefone
) {}