package com.gabrielbl.healthaplication.model.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AvaliacaoMensalComSetoresResponseDTO(
        String id,
        LocalDateTime criadoEm,
        boolean isActive,
        EmpresaResponseDTO empresa,
        List<FuncionarioDTO> funcionario
) {
}
