package com.gabrielbl.healthaplication.model.DTOs;


import com.gabrielbl.healthaplication.model.UsuarioFuncao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegistrarRhEEmpresaDTO(@NotBlank String nome, @NotBlank  String login,
                                     @NotBlank String password, @NotNull UsuarioFuncao role,
                                     @NotBlank @Pattern(regexp = "\\d{14}")  String cnpj,
                                     @NotBlank String nomeEmpresa,
                                     @NotBlank String telefoneEmpresa,
                                     @NotBlank String emailEmpresa) {
}