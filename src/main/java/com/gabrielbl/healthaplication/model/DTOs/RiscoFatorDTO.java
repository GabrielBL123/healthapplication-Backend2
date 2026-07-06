package com.gabrielbl.healthaplication.model.DTOs;

public record RiscoFatorDTO(
        int numeroFator,
        String nomeFator,
        double percepcao,        // P  — média das 2 perguntas de percepção
        double condicao,         // C  — média das 2 perguntas de condição (original)
        double condicaoAjustada, // C_ajustada = 6 - C
        double risco,            // R  = (P + C_ajustada) / 2
        String classificacao,    // "Baixo" | "Médio" | "Alto" | "Crítico"
        String alertaEspecial    // null ou descrição da regra de alerta disparada
) {}
