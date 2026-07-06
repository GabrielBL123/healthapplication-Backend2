package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;

public record RiscoSetorDTO(
        String setorNome,
        int totalRespondentes,          // quantos funcionários responderam neste setor
        boolean exibirResultado,        // false quando totalRespondentes < 3
        double riscoGeralSetor,         // média dos 13 riscos do setor (só calculado quando exibirResultado=true)
        String classificacaoGeral,      // classificação do risco geral do setor
        List<RiscoFatorDTO> fatores     // lista com os 13 fatores (só populada quando exibirResultado=true)
) {}
