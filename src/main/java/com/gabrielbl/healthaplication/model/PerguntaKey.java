package com.gabrielbl.healthaplication.model;

import lombok.Getter;

@Getter
public enum PerguntaKey {

    F1_P1(1, 1);

    private final short factor;
    private final short numero;

    PerguntaKey(int factor, int numero) {
        this.factor = (short) factor;
        this.numero = (short) numero;
    }

    public String key() { return name(); }
}
