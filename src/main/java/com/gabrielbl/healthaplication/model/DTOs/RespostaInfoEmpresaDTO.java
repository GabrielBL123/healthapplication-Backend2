package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;


import java.util.List;

public record RespostaInfoEmpresaDTO(
        @NotBlank String empresaNome,
        @NotBlank String empresaCnpj,
        List<String> nomeSetor
) {
}
