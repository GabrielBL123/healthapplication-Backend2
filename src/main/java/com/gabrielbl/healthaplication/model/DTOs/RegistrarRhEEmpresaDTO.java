package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarRhEEmpresaDTO(
        @NotBlank String nome,
        @NotBlank String login,
        @NotBlank String password,
        @NotNull UsuarioFuncao role,
        @NotBlank String cnpj,
        @NotBlank String nomeEmpresa,
        @NotBlank String emailEmpresa,
        @NotBlank String telefoneEmpresa
) {
}