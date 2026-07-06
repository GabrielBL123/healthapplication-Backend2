package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.NotBlank;

public record CnpjRequest( @NotBlank String cnpj) {

}
