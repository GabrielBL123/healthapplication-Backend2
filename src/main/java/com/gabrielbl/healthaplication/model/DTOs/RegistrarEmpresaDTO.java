package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrarEmpresaDTO(@NotBlank @Pattern(regexp = "\\d{14}") String cnpj,
                                  @NotBlank String nome,
                                  @NotBlank String email,
                                  @NotBlank String telefone) {
}
