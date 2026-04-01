package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.PerguntaKey;
import com.gabrielbl.healthaplication.model.Resposta;


import java.util.EnumMap;
import java.util.Map;

public record CriarPedidoQuestionario( Integer competence, // YYYYMM, ex 202603

                                      /**
                                        * answers: { "F1_P1": 3, "F1_P2": 5, ... }
                                        *
                                        * Usar EnumMap deixa mais eficiente.
                                        * O Jackson consegue desserializar chaves enum pelo nome ("F1_P1").
                                        */
                                       Map<PerguntaKey, Resposta> answers
) {
    public Map<PerguntaKey, Resposta> answers() {
        // garante estrutura boa internamente (opcional)
        return (answers instanceof EnumMap<PerguntaKey, Resposta>)
                ? answers
                : new EnumMap<>(answers);
    }
}
