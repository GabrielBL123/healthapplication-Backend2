package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;

public record RiscoEmpresaDTO(
        String nomeEmpresa,
        String cnpj,
        int totalRespondentesGeral,     // soma de todos os funcionários que responderam na empresa
        double riscoGeralEmpresa,       // média de todos os 13 fatores considerando a empresa inteira
        String classificacaoGeral,      // "Baixo" | "Médio" | "Alto" | "Crítico"
        List<RiscoFatorDTO> fatoresGlobais, // visão macro da empresa para os 13 fatores
        List<RiscoSetorDTO> setores     // array com o detalhamento que você já criou, setor por setor
) {}
