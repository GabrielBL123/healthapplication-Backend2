package com.gabrielbl.healthaplication.model;

import lombok.Getter;

@Getter
public enum UsuarioFuncao {

    ADMIN("admin"),
    USER("user"),
    RH("rh");


    private final String role;

    UsuarioFuncao(String role) {
        this.role = role;
    }


    ;

}
