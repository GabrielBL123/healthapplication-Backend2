package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;

public record RegisterCompanyDTO(String cnpj, String nome, String email, String telefone) {
}
